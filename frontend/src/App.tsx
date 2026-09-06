import { AppBar, Box, Container, Toolbar, Typography } from '@mui/material'

/** Page shell: title bar, the play area (added in the next tickets) and a footer. */
export function App() {
  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" component="header">
        <Toolbar>
          <Typography variant="h1" component="h1">
            Dragons of Mugloar
          </Typography>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ flex: 1, py: 3 }}>
        <Typography>Start a game, pick the ads your dragon should solve, and buy items in the shop.</Typography>
      </Container>
      <Box component="footer" sx={{ py: 2, textAlign: 'center', color: 'text.secondary' }}>
        <Typography variant="body2">A take-home assignment for the Dragons of Mugloar game.</Typography>
      </Box>
    </Box>
  )
}
