const fs = require('fs')
const zlib = require('zlib')
const { Vibrant } = require('node-vibrant/node')

const SURFACE = [255, 255, 255]
const MIX = 0.16
const cache = new Map()
let webpDecoder

function mixSurface(rgb) {
  const channel = (index) => Math.round(SURFACE[index] * (1 - MIX) + rgb[index] * MIX)
  return `rgb(${channel(0)}, ${channel(1)}, ${channel(2)})`
}

function isWebp(buffer) {
  return buffer.length > 12
    && buffer.toString('ascii', 0, 4) === 'RIFF'
    && buffer.toString('ascii', 8, 12) === 'WEBP'
}

function crc32(buf) {
  let crc = ~0
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i]
    for (let bit = 0; bit < 8; bit++) {
      crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0)
    }
  }
  return ~crc >>> 0
}

function pngChunk(type, data) {
  const typeBuf = Buffer.from(type)
  const length = Buffer.alloc(4)
  length.writeUInt32BE(data.length)
  const crc = Buffer.alloc(4)
  crc.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])))
  return Buffer.concat([length, typeBuf, data, crc])
}

function rgbaToPng(width, height, rgba) {
  const raw = Buffer.alloc((width * 4 + 1) * height)
  const pixels = Buffer.from(rgba.buffer, rgba.byteOffset, rgba.byteLength)
  for (let y = 0; y < height; y++) {
    const dest = y * (width * 4 + 1)
    raw[dest] = 0
    pixels.copy(raw, dest + 1, y * width * 4, (y + 1) * width * 4)
  }
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(width, 0)
  ihdr.writeUInt32BE(height, 4)
  ihdr[8] = 8
  ihdr[9] = 6
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    pngChunk('IHDR', ihdr),
    pngChunk('IDAT', zlib.deflateSync(raw)),
    pngChunk('IEND', Buffer.alloc(0))
  ])
}

async function decodeWebp(buffer) {
  if (!webpDecoder) {
    const imported = await import('@jsquash/webp/decode.js')
    const wasmModule = await WebAssembly.compile(fs.readFileSync(require.resolve('@jsquash/webp/codec/dec/webp_dec.wasm')))
    await imported.init(wasmModule)
    webpDecoder = imported.default
  }
  return webpDecoder(buffer)
}

function isIco(buffer) {
  return buffer.length > 6 && buffer.readUInt16LE(0) === 0 && buffer.readUInt16LE(2) === 1
}

function dibToRgba(dib) {
  if (dib.length < 40 || dib.readUInt32LE(0) < 40) return null
  const headerSize = dib.readUInt32LE(0)
  const width = dib.readInt32LE(4)
  const fullHeight = Math.abs(dib.readInt32LE(8))
  const height = Math.floor(fullHeight / 2)
  const bitCount = dib.readUInt16LE(14)
  if (width < 1 || height < 1 || bitCount !== 32) return null
  const xor = dib.subarray(headerSize)
  const pixels = Buffer.alloc(width * height * 4)
  const stride = width * 4
  for (let y = 0; y < height; y++) {
    const srcY = height - 1 - y
    for (let x = 0; x < width; x++) {
      const src = (srcY * stride) + x * 4
      const dest = (y * width + x) * 4
      pixels[dest] = xor[src + 2]
      pixels[dest + 1] = xor[src + 1]
      pixels[dest + 2] = xor[src]
      pixels[dest + 3] = xor[src + 3]
    }
  }
  return { width, height, data: pixels }
}

function icoToPng(buffer) {
  const count = buffer.readUInt16LE(4)
  let best = null
  for (let i = 0; i < count; i++) {
    const entry = 6 + i * 16
    const width = buffer[entry] || 256
    const bytes = buffer.readUInt32LE(entry + 8)
    const offset = buffer.readUInt32LE(entry + 12)
    if (offset + bytes > buffer.length) continue
    const image = buffer.subarray(offset, offset + bytes)
    if (!best || width > best.width) best = { width, image }
  }
  if (!best) return null
  if (best.image.subarray(0, 8).equals(Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]))) {
    return best.image
  }
  const rgba = dibToRgba(best.image)
  return rgba ? rgbaToPng(rgba.width, rgba.height, rgba.data) : null
}

async function paletteFromBuffer(buffer) {
  let image = buffer
  if (isWebp(buffer)) {
    const decoded = await decodeWebp(buffer)
    image = rgbaToPng(decoded.width, decoded.height, decoded.data)
  } else if (isIco(buffer)) {
    const png = icoToPng(buffer)
    if (!png) return null
    image = png
  }
  return Vibrant.from(image).getPalette()
}

function dominant(palette) {
  return Object.values(palette)
    .filter(Boolean)
    .sort((a, b) => b.population - a.population)[0] || null
}

async function highlightForIconUrl(iconUrl) {
  if (!iconUrl || typeof iconUrl !== 'string') return null
  if (cache.has(iconUrl)) return cache.get(iconUrl)

  try {
    const parsed = new URL(iconUrl)
    if (parsed.protocol !== 'https:' || parsed.hostname !== 'favicon.im') return null
    const response = await fetch(parsed.toString())
    if (!response.ok) return null
    const buffer = Buffer.from(await response.arrayBuffer())
    const palette = await paletteFromBuffer(buffer)
    const swatch = palette.Vibrant || palette.DarkVibrant || palette.Muted || dominant(palette)
    const color = swatch ? mixSurface(swatch.rgb) : null
    if (color) cache.set(iconUrl, color)
    return color
  } catch (error) {
    return null
  }
}

module.exports = { highlightForIconUrl }
