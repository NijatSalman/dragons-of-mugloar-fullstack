import { Alert, Snackbar } from '@mui/material'
import type { Notice } from '../state/gameReducer'

/** The game server's own words about the last turn, shown briefly near the bottom of the screen. */
export function NoticeBar({ notice, onDismiss }: { notice: Notice; onDismiss: () => void }) {
  return (
    <Snackbar open autoHideDuration={4000} onClose={onDismiss} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
      <Alert
        severity={notice.tone}
        variant="filled"
        onClose={onDismiss}
        sx={{ fontSize: '1.05rem', fontWeight: 600, color: '#15110d', '& .MuiAlert-icon, & .MuiAlert-action': { color: '#15110d' } }}
      >
        {notice.message}
      </Alert>
    </Snackbar>
  )
}
