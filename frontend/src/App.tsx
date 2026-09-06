/** Page shell: title, the play area (added in the next tickets) and a footer. */
export function App() {
  return (
    <div className="app">
      <header className="app__header">
        <h1 className="app__title">Dragons of Mugloar</h1>
      </header>
      <main className="app__main">
        <p>Start a game, pick the ads your dragon should solve, and buy items in the shop.</p>
      </main>
      <footer className="app__footer">A take-home assignment for the Dragons of Mugloar game.</footer>
    </div>
  )
}
