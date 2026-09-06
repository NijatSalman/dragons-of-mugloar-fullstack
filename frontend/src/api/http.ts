import type { ProblemDetail } from './types'

/** What a failed call looks like to the UI: the HTTP status, a message to show, and the traceId to report. */
export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly detail: string
  readonly traceId?: string

  constructor(status: number, title: string, detail: string, traceId?: string) {
    super(detail)
    this.name = 'ApiError'
    this.status = status
    this.title = title
    this.detail = detail
    this.traceId = traceId
  }
}

/**
 * Sends one request to our backend and returns the parsed JSON body. Any non-2xx answer becomes an ApiError
 * built from the ProblemDetail body; a network failure becomes an ApiError with status 0.
 */
export async function request<T>(method: 'GET' | 'POST', url: string): Promise<T> {
  let response: Response
  try {
    response = await fetch(url, { method, headers: { Accept: 'application/json' } })
  } catch {
    throw new ApiError(0, 'Network error', 'The server could not be reached')
  }
  if (!response.ok) {
    throw await toApiError(response)
  }
  return (await response.json()) as T
}

async function toApiError(response: Response): Promise<ApiError> {
  const problem = await readProblem(response)
  return new ApiError(
    response.status,
    problem?.title ?? response.statusText,
    problem?.detail ?? `Request failed with status ${response.status}`,
    problem?.traceId ?? response.headers.get('X-Trace-Id') ?? undefined,
  )
}

async function readProblem(response: Response): Promise<ProblemDetail | undefined> {
  try {
    return (await response.json()) as ProblemDetail
  } catch {
    return undefined
  }
}
