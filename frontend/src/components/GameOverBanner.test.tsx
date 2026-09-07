import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { finishedGame } from '../test/fixtures'
import { GameOverBanner } from './GameOverBanner'

describe('GameOverBanner', () => {
  it('gameOverBannerShowsTheFinalScoreAndTurns', () => {
    render(<GameOverBanner game={finishedGame} onStartAgain={vi.fn()} />)

    expect(screen.getByText(/201 turns/)).toBeInTheDocument()
    expect(screen.getByText('5239')).toBeInTheDocument()
  })

  it('gameOverBannerStartAgainButtonResetsTheGame', async () => {
    const onStartAgain = vi.fn()
    render(<GameOverBanner game={finishedGame} onStartAgain={onStartAgain} />)

    await userEvent.click(screen.getByRole('button', { name: 'Start again' }))

    expect(onStartAgain).toHaveBeenCalledOnce()
  })
})
