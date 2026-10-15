import { NextRequest, NextResponse } from 'next/server'

const ALLOWED_HOST = 'favicon.im'

export async function GET(request: NextRequest) {
  const target = request.nextUrl.searchParams.get('url')
  if (!target) {
    return new NextResponse('missing url', { status: 400 })
  }

  let parsed: URL
  try {
    parsed = new URL(target)
  } catch {
    return new NextResponse('bad url', { status: 400 })
  }

  if (parsed.protocol !== 'https:' || parsed.hostname !== ALLOWED_HOST) {
    return new NextResponse('forbidden', { status: 403 })
  }

  const upstream = await fetch(parsed.toString())
  if (!upstream.ok) {
    return new NextResponse('upstream failed', { status: 502 })
  }

  return new NextResponse(await upstream.arrayBuffer(), {
    headers: {
      'Content-Type': upstream.headers.get('content-type') || 'image/png',
      'Cache-Control': 'public, max-age=86400'
    }
  })
}
