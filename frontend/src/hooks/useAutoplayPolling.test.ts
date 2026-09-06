import { renderHook } from '@testing-library/react'
import type { AutoplaySession } from '../api/types'
import { POLL_INTERVAL_MS, useAutoplayPolling } from './useAutoplayPolling'

const session = (status: 'RUNNING' | 'FINISHED'): AutoplaySession => ({
  sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
  status,
  startedAt: '2026-09-06T14:00:00Z',
  requested: 1,
  finished: status === 'FINISHED' ? 1 : 0,
  games: [],
})

describe('useAutoplayPolling', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('refreshesARunningSessionEveryInterval', () => {
    const refresh = vi.fn().mockResolvedValue(undefined)
    renderHook(() => useAutoplayPolling(session('RUNNING'), refresh))

    vi.advanceTimersByTime(POLL_INTERVAL_MS * 2)

    expect(refresh).toHaveBeenCalledTimes(2)
    expect(refresh).toHaveBeenCalledWith('84e1bfd2-3f61-467d-8fe5-90b9bc358e39')
  })

  it('stopsOnceTheSessionIsFinished', () => {
    const refresh = vi.fn().mockResolvedValue(undefined)
    renderHook(() => useAutoplayPolling(session('FINISHED'), refresh))

    vi.advanceTimersByTime(POLL_INTERVAL_MS * 3)

    expect(refresh).not.toHaveBeenCalled()
  })

  it('doesNothingWithoutASession', () => {
    const refresh = vi.fn().mockResolvedValue(undefined)
    renderHook(() => useAutoplayPolling(undefined, refresh))

    vi.advanceTimersByTime(POLL_INTERVAL_MS)

    expect(refresh).not.toHaveBeenCalled()
  })
})
