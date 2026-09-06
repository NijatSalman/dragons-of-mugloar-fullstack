import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithGame } from '../test/renderWithGame'
import { StartPanel } from './StartPanel'

describe('StartPanel', () => {
  it('startButtonStartsAGame', async () => {
    const { actions } = renderWithGame(<StartPanel />)

    await userEvent.click(screen.getByRole('button', { name: 'Start new game' }))

    expect(actions.startGame).toHaveBeenCalledOnce()
  })

  it('startButtonIsDisabledWhileARequestIsRunning', () => {
    renderWithGame(<StartPanel />, { busy: true })

    expect(screen.getByRole('button', { name: 'Start new game' })).toBeDisabled()
  })
})
