module.exports = {
  extends: ['@commitlint/config-conventional'],
  ignores: [
    // Dependabot commit messages include 'updated-dependencies:' in the footer
    (message) => message.includes('updated-dependencies:')
  ]
}
