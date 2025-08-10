import ReactDOM from 'react-dom/client'

import { Provider } from 'react-redux'
import { store } from './store/store'
import App from './App'
import { AuthProvider } from 'react-oauth2-code-pkce'
import { authConfig } from './authConfig'



// As of React 18, the createRoot API is the recommended way to render a React application.
const root = ReactDOM.createRoot(document.getElementById('root'))

root.render(
  <AuthProvider authConfig={authConfig} lodingComponent={<div>Loading...</div>}>
    <Provider store={store}>
      <App />
    </Provider>
  </AuthProvider>
)
