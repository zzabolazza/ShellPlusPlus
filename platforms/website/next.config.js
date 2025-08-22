/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  output: 'standalone',
  images: {
    domains: ['favicon.im'],
    unoptimized: true
  },
  trailingSlash: false,
  async redirects() {
    return []
  },
  async rewrites() {
    return []
  }
}

module.exports = nextConfig
