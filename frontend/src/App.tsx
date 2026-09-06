import { AppBar, Box, Container, Stack, Tab, Tabs, Toolbar, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { AdBoard } from './components/AdBoard'
import { AutoplayPanel } from './components/AutoplayPanel'
import { DragonMark } from './components/DragonMark'
import { ErrorBanner } from './components/ErrorBanner'
import { GameOverBanner } from './components/GameOverBanner'
import { LeaderboardPanel } from './components/LeaderboardPanel'
import { NoticeBar } from './components/NoticeBar'
import { ShopPanel } from './components/ShopPanel'
import { StartPanel } from './components/StartPanel'
import { StatusBar } from './components/StatusBar'
import { useAutoplayPolling } from './hooks/useAutoplayPolling'
import { useGame } from './state/useGame'

type Tab = 'play' | 'autoplay' | 'leaderboard'

/** Page shell: title bar, three tabs (play by hand, let the dragon play, leaderboard) and a footer. */
export function App() {
  const { state, actions } = useGame()
  const [tab, setTab] = useState<Tab>('play')
  const playing = state.game !== undefined && !state.game.over
  useAutoplayPolling(state.session, actions.refreshAutoplay)

  // The leaderboard is fetched whenever its tab is opened.
  useEffect(() => {
    if (tab === 'leaderboard') void actions.loadLeaderboard()
  }, [tab, actions])

  // The catalogue is the same for every game; load it once a game exists.
  useEffect(() => {
    if (playing && state.shop.length === 0) void actions.loadShop()
  }, [playing, state.shop.length, actions])

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" component="header">
        <Toolbar sx={{ gap: 1.5, flexWrap: 'wrap' }}>
          <DragonMark sx={{ fontSize: 40, color: 'primary.main' }} />
          <Typography variant="h1" component="h1" sx={{ color: 'primary.main', flex: 1 }}>
            Dragons of Mugloar
          </Typography>
          <Tabs value={tab} onChange={(_event, next: Tab) => setTab(next)} textColor="inherit" indicatorColor="primary">
            <Tab value="play" label="Play" />
            <Tab value="autoplay" label="Autoplay" />
            <Tab value="leaderboard" label="Leaderboard" />
          </Tabs>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ flex: 1, py: 3 }}>
        <Stack spacing={2}>
          {state.error && <ErrorBanner error={state.error} onDismiss={actions.dismissError} />}
          {tab === 'play' && <PlayTab />}
          {tab === 'autoplay' && (
            <AutoplayPanel session={state.session} disabled={state.busy} onStart={(games) => void actions.startAutoplay(games)} />
          )}
          {tab === 'leaderboard' && (
            <LeaderboardPanel games={state.leaderboard} disabled={state.busy} onRefresh={() => void actions.loadLeaderboard()} />
          )}
        </Stack>
      </Container>
      {state.notice && <NoticeBar notice={state.notice} onDismiss={actions.dismissNotice} />}
      <Box component="footer" sx={{ py: 2, textAlign: 'center', color: 'text.secondary', borderTop: '1px solid rgba(212,165,58,0.25)' }}>
        <Typography variant="body2">Every dragon needs an errand. Choose wisely.</Typography>
      </Box>
    </Box>
  )
}

/** Playing by hand: start panel, or status bar with the board and the shop, or the game-over card. */
function PlayTab() {
  const { state, actions } = useGame()
  if (!state.game) return <StartPanel />
  const game = state.game
  return (
    <>
      <StatusBar game={game} onLeave={actions.resetGame} />
      {game.over && <GameOverBanner game={game} onStartAgain={actions.resetGame} />}
      {!game.over && (
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '2fr 1fr' }, alignItems: 'start' }}>
          <AdBoard ads={state.ads} disabled={state.busy} onSolve={(adId) => void actions.solveAd(adId)} onRefresh={() => void actions.refreshAds()} />
          <ShopPanel items={state.shop} gold={game.gold} disabled={state.busy} onBuy={(itemId) => void actions.buyItem(itemId)} />
        </Box>
      )}
    </>
  )
}
