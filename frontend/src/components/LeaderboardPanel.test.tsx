import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { GameSummary } from '../api/types'
import { LeaderboardPanel } from './LeaderboardPanel'

const games: GameSummary[] = [
  { gameId: 'jz21oOWI', origin: 'AUTOPLAY', score: 5239, turn: 201, lives: 0, gold: 87, level: 3 },
  { gameId: 'ggLmesXI', origin: 'MANUAL', score: 1462, turn: 41, lives: 0, gold: 12, level: 1 },
]

describe('LeaderboardPanel', () => {
  it('ranksGamesAndSaysHowEachWasPlayed', () => {
    render(<LeaderboardPanel games={games} disabled={false} onRefresh={vi.fn()} />)

    const rows = screen.getAllByRole('row').slice(1)
    expect(rows[0]).toHaveTextContent('1jz21oOWIautoplay5239201873')
    expect(rows[1]).toHaveTextContent('2ggLmesXImanual146241121')
  })

  it('refreshButtonReloadsTheBoard', async () => {
    const onRefresh = vi.fn()
    render(<LeaderboardPanel games={games} disabled={false} onRefresh={onRefresh} />)

    await userEvent.click(screen.getByRole('button', { name: 'Refresh leaderboard' }))

    expect(onRefresh).toHaveBeenCalledOnce()
  })

  it('explainsAnEmptyLeaderboard', () => {
    render(<LeaderboardPanel games={[]} disabled={false} onRefresh={vi.fn()} />)

    expect(screen.getByText(/No finished games yet/)).toBeInTheDocument()
  })
})
