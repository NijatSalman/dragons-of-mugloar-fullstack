import { useEffect } from 'react'
import type { AutoplaySession } from '../api/types'

export const POLL_INTERVAL_MS = 3000

/** While an autoplay session is running, asks the backend for progress every few seconds. */
export function useAutoplayPolling(session: AutoplaySession | undefined, refresh: (sessionId: string) => Promise<void>) {
  const sessionId = session?.status === 'RUNNING' ? session.sessionId : undefined

  useEffect(() => {
    if (!sessionId) return
    const timer = setInterval(() => void refresh(sessionId), POLL_INTERVAL_MS)
    return () => clearInterval(timer)
  }, [sessionId, refresh])
}
