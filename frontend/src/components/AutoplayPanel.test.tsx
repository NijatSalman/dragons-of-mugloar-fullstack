import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { AutoplaySession } from '../api/types'
import { AutoplayPanel } from './AutoplayPanel'

const runningSession: AutoplaySession = {
  sessionId: '84e1bfd2-3f61-467d-8fe5-90b9bc358e39',
  status: 'RUNNING',
  startedAt: '2026-09-06T14:00:00Z',
  requested: 3,
  finished: 1,
  games: [
    { gameId: 'jz21oOWI', status: 'FINISHED', score: 5239, turn: 201, lives: 0 },
    { gameId: '8ZW2dZlT', status: 'RUNNING', score: 2870, turn: 120, lives: 2 },
    { status: 'RUNNING', score: 0, turn: 0, lives: 0 },
  ],
  summary: { min: 5239, avg: 5239, max: 5239 },
}

describe('AutoplayPanel', () => {
  it('startButtonReportsTheChosenNumberOfGames', async () => {
    const onStart = vi.fn()
    render(<AutoplayPanel disabled={false} onStart={onStart} />)

    const input = screen.getByRole('spinbutton', { name: 'Number of games' })
    await userEvent.clear(input)
    await userEvent.type(input, '5')
    await userEvent.click(screen.getByRole('button', { name: 'Start autoplay' }))

    expect(onStart).toHaveBeenCalledWith(5)
  })

  it('startButtonIsDisabledForAnInvalidNumberOfGames', async () => {
    render(<AutoplayPanel disabled={false} onStart={vi.fn()} />)

    const input = screen.getByRole('spinbutton', { name: 'Number of games' })
    await userEvent.clear(input)
    await userEvent.type(input, '25')

    expect(screen.getByRole('button', { name: 'Start autoplay' })).toBeDisabled()
    expect(screen.getByText('1 to 20')).toBeInTheDocument()
  })

  it('startButtonIsDisabledWhileASessionIsRunning', () => {
    render(<AutoplayPanel session={runningSession} disabled={false} onStart={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Start autoplay' })).toBeDisabled()
  })

  it('showsProgressOfEveryGameAndTheSummary', () => {
    render(<AutoplayPanel session={runningSession} disabled={false} onStart={vi.fn()} />)

    expect(screen.getByText('1 of 3 games finished')).toBeInTheDocument()
    expect(screen.getByText('jz21oOWI')).toBeInTheDocument()
    expect(screen.getByText('5239')).toBeInTheDocument()
    expect(screen.getByText('starting…')).toBeInTheDocument()
    expect(screen.getByText('scores: min 5239 · avg 5239 · max 5239')).toBeInTheDocument()
  })
})
