import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { App } from './App'
import { ApiError } from './api/http'
import { finishedGame, quiteLikelyAd, runningGame } from './test/fixtures'
import { renderWithGame } from './test/renderWithGame'

describe('App', () => {
  it('appShowsTheStartPanelWhenNoGameIsRunning', () => {
    renderWithGame(<App />)

    expect(screen.getByRole('heading', { name: 'Dragons of Mugloar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start new game' })).toBeInTheDocument()
  })

  it('appShowsALoadingHintInsteadOfTheStartPanelWhileRestoring', () => {
    renderWithGame(<App />, { restoring: true })

    expect(screen.getByText('Loading your game…')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Start new game' })).not.toBeInTheDocument()
  })

  it('appShowsStatusBarAndAdBoardWhenAGameIsRunning', () => {
    renderWithGame(<App />, { game: runningGame, ads: [quiteLikelyAd] })

    expect(screen.getByText('3 lives')).toBeInTheDocument()
    expect(screen.getByText(quiteLikelyAd.message)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Start new game' })).not.toBeInTheDocument()
  })

  it('appLoadsTheShopOnceAGameIsRunning', () => {
    const { actions } = renderWithGame(<App />, { game: runningGame })

    expect(actions.loadShop).toHaveBeenCalledOnce()
  })

  it('appShowsTheShopNextToTheBoard', () => {
    renderWithGame(<App />, { game: runningGame, shop: [{ itemId: 'hpot', name: 'Healing potion', cost: 50 }] })

    expect(screen.getByRole('region', { name: 'Shop' })).toBeInTheDocument()
    expect(screen.getByText('Healing potion')).toBeInTheDocument()
  })

  it('appShowsGameOverInsteadOfTheBoardWhenLivesAreGone', () => {
    renderWithGame(<App />, { game: finishedGame, ads: [quiteLikelyAd] })

    expect(screen.getByRole('heading', { name: 'Game over' })).toBeInTheDocument()
    expect(screen.queryByText(quiteLikelyAd.message)).not.toBeInTheDocument()
  })

  it('appShowsTheServersNoticeAfterATurn', () => {
    renderWithGame(<App />, { game: runningGame, notice: { message: 'You successfully solved the mission!', tone: 'success' } })

    expect(screen.getByText('You successfully solved the mission!')).toBeInTheDocument()
  })

  it('appShowsTheAutoplayPanelOnItsTab', async () => {
    renderWithGame(<App />)

    await userEvent.click(screen.getByRole('tab', { name: 'Autoplay' }))

    expect(screen.getByRole('region', { name: 'Autoplay' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Start new game' })).not.toBeInTheDocument()
  })

  it('appLoadsAndShowsTheLeaderboardOnItsTab', async () => {
    const { actions } = renderWithGame(<App />, {
      leaderboard: [{ gameId: 'jz21oOWI', origin: 'AUTOPLAY', score: 5239, turn: 201, lives: 0, gold: 87, level: 3 }],
    })

    await userEvent.click(screen.getByRole('tab', { name: 'Top scores' }))

    expect(actions.loadLeaderboard).toHaveBeenCalled()
    expect(screen.getByText('jz21oOWI')).toBeInTheDocument()
  })

  it('appOpensTheTabNamedInTheUrlHash', () => {
    window.location.hash = '#autoplay'

    renderWithGame(<App />)

    expect(screen.getByRole('region', { name: 'Autoplay' })).toBeInTheDocument()
    window.location.hash = ''
  })

  it('appLeaveGameReturnsToTheStartPanel', async () => {
    const { actions } = renderWithGame(<App />, { game: runningGame })

    await userEvent.click(screen.getByRole('button', { name: 'Leave game' }))

    expect(actions.resetGame).toHaveBeenCalledOnce()
  })

  it('appShowsTheErrorBannerWithTheTraceId', () => {
    renderWithGame(<App />, {
      error: new ApiError(502, 'Bad Gateway', 'Game server is currently unavailable', '4bf92f3577b34da6a3ce929d0e0e4736'),
    })

    expect(screen.getByText(/Game server is currently unavailable/)).toBeInTheDocument()
    expect(screen.getByText(/4bf92f3577b34da6a3ce929d0e0e4736/)).toBeInTheDocument()
  })
})
