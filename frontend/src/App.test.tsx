import { render, screen } from '@testing-library/react'
import { App } from './App'

describe('App', () => {
  it('rendersTheTitle', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Dragons of Mugloar' })).toBeInTheDocument()
  })
})
