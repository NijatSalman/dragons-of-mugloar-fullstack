import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { GameSummary } from '../api/types'
import { LeaderboardPanel } from './LeaderboardPanel'

const games: GameSummary[] = [
  { gameId: 'jz21oOWI', origin: 'AUTOPLAY', score: 5239, turn: 201, lives: 0, gold: 87, level: 3, over: true },
  { gameId: 'ggLmesXI', origin: 'MANUAL', score: 300, turn: 15, lives: 3, gold: 120, level: 1, over: false },
]

describe('LeaderboardPanel', () => {
  it('ranksGamesWithWhoPlayedAndTheirState', () => {
    render(<LeaderboardPanel games={games} disabled={false} onRefresh={vi.fn()} />)

    const rows = screen.getAllByRole('row').slice(1)
    expect(rows[0]).toHaveTextContent('1jz21oOWIdragon5239201087')
    expect(rows[0]).toHaveTextContent('finished')
    expect(rows[1]).toHaveTextContent('you')
    expect(rows[1]).toHaveTextContent('running')
  })

  it('refreshButtonReloadsTheBoard', async () => {
    const onRefresh = vi.fn()
    render(<LeaderboardPanel games={games} disabled={false} onRefresh={onRefresh} />)

    await userEvent.click(screen.getByRole('button', { name: 'Refresh leaderboard' }))

    expect(onRefresh).toHaveBeenCalledOnce()
  })

  it('explainsAnEmptyLeaderboard', () => {
    render(<LeaderboardPanel games={[]} disabled={false} onRefresh={vi.fn()} />)

    expect(screen.getByText(/No games yet/)).toBeInTheDocument()
  })
})
