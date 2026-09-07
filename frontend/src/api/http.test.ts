import { ApiError, request } from './http'

function jsonResponse(status: number, body: unknown, headers: Record<string, string> = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json', ...headers },
  })
}

describe('request', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('requestReturnsTheParsedBodyOnSuccess', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, { gameId: 'ggLmesXI', lives: 3 }))

    const body = await request<{ gameId: string; lives: number }>('GET', '/api/v1/games/ggLmesXI')

    expect(body).toEqual({ gameId: 'ggLmesXI', lives: 3 })
    expect(fetch).toHaveBeenCalledWith('/api/v1/games/ggLmesXI', {
      method: 'GET',
      headers: { Accept: 'application/json' },
    })
  })

  it('requestThrowsApiErrorBuiltFromTheProblemDetail', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(404, {
        status: 404,
        title: 'Not Found',
        detail: 'Game not found: gameId=nope1234',
        traceId: '4bf92f3577b34da6a3ce929d0e0e4736',
      }),
    )

    const error = await request('GET', '/api/v1/games/nope1234').catch((caught: ApiError) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      status: 404,
      title: 'Not Found',
      detail: 'Game not found: gameId=nope1234',
      traceId: '4bf92f3577b34da6a3ce929d0e0e4736',
    })
  })

  it('requestThrowsApiErrorWithStatusZeroWhenTheNetworkFails', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'))

    const error = await request('GET', '/api/v1/games').catch((caught: ApiError) => caught)

    expect(error).toMatchObject({ status: 0, title: 'Connection problem' })
  })

  it('requestFallsBackToStatusTextWhenTheBodyIsNotJson', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response('<html>Bad Gateway</html>', { status: 502, statusText: 'Bad Gateway' }),
    )

    const error = await request('GET', '/api/v1/games').catch((caught: ApiError) => caught)

    expect(error).toMatchObject({ status: 502, title: 'Bad Gateway', detail: 'Request failed with status 502' })
  })
})
