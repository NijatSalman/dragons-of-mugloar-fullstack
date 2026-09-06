import { Alert, AlertTitle } from '@mui/material'
import type { ApiError } from '../api/http'

/** Shows what went wrong and the traceId that finds it in the server logs. */
export function ErrorBanner({ error, onDismiss }: { error: ApiError; onDismiss: () => void }) {
  return (
    <Alert severity="error" onClose={onDismiss}>
      <AlertTitle>{error.title}</AlertTitle>
      {error.detail}
      {error.traceId && <> (trace {error.traceId})</>}
    </Alert>
  )
}
