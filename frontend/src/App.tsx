import { AppBar, Box, Container, Stack, Toolbar, Typography } from '@mui/material'
import { DragonMark } from './components/DragonMark'
import { ErrorBanner } from './components/ErrorBanner'
import { StartPanel } from './components/StartPanel'
import { StatusBar } from './components/StatusBar'
import { useGame } from './state/useGame'

/** Page shell: title bar, the play area and a footer. Shows the start panel until a game is running. */
export function App() {
  const { state, actions } = useGame()

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" component="header">
        <Toolbar sx={{ gap: 1.5 }}>
          <DragonMark sx={{ fontSize: 40, color: 'primary.main' }} />
          <Typography variant="h1" component="h1" sx={{ color: 'primary.main' }}>
            Dragons of Mugloar
          </Typography>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ flex: 1, py: 3 }}>
        <Stack spacing={2}>
          {state.error && <ErrorBanner error={state.error} onDismiss={actions.dismissError} />}
          {state.game ? <StatusBar game={state.game} /> : <StartPanel />}
        </Stack>
      </Container>
      <Box component="footer" sx={{ py: 2, textAlign: 'center', color: 'text.secondary', borderTop: '1px solid rgba(212,165,58,0.25)' }}>
        <Typography variant="body2">Every dragon needs an errand. Choose wisely.</Typography>
      </Box>
    </Box>
  )
}
