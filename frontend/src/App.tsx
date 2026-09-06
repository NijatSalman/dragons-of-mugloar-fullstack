import { AppBar, Box, Container, Stack, Toolbar, Typography } from '@mui/material'
import { useEffect } from 'react'
import { AdBoard } from './components/AdBoard'
import { AutoplayPanel } from './components/AutoplayPanel'
import { DragonMark } from './components/DragonMark'
import { ErrorBanner } from './components/ErrorBanner'
import { GameOverBanner } from './components/GameOverBanner'
import { NoticeBar } from './components/NoticeBar'
import { ShopPanel } from './components/ShopPanel'
import { StartPanel } from './components/StartPanel'
import { StatusBar } from './components/StatusBar'
import { useAutoplayPolling } from './hooks/useAutoplayPolling'
import { useGame } from './state/useGame'

/** Page shell: title bar, the play area and a footer. Shows the start panel until a game is running. */
export function App() {
  const { state, actions } = useGame()
  const playing = state.game !== undefined && !state.game.over
  useAutoplayPolling(state.session, actions.refreshAutoplay)

  // The catalogue is the same for every game; load it once a game exists.
  useEffect(() => {
    if (playing && state.shop.length === 0) void actions.loadShop()
  }, [playing, state.shop.length, actions])

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
          {!state.game && <StartPanel />}
          {state.game && <StatusBar game={state.game} />}
          {state.game?.over && <GameOverBanner game={state.game} onStartAgain={actions.resetGame} />}
          {playing && state.game && (
            <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '2fr 1fr' }, alignItems: 'start' }}>
              <AdBoard ads={state.ads} disabled={state.busy} onSolve={(adId) => void actions.solveAd(adId)} onRefresh={() => void actions.refreshAds()} />
              <ShopPanel items={state.shop} gold={state.game.gold} disabled={state.busy} onBuy={(itemId) => void actions.buyItem(itemId)} />
            </Box>
          )}
          <AutoplayPanel session={state.session} disabled={state.busy} onStart={(games) => void actions.startAutoplay(games)} />
        </Stack>
      </Container>
      {state.notice && <NoticeBar notice={state.notice} onDismiss={actions.dismissNotice} />}
      <Box component="footer" sx={{ py: 2, textAlign: 'center', color: 'text.secondary', borderTop: '1px solid rgba(212,165,58,0.25)' }}>
        <Typography variant="body2">Every dragon needs an errand. Choose wisely.</Typography>
      </Box>
    </Box>
  )
}
