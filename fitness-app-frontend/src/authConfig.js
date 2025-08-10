export const authConfig = {
    clientId: 'oauth2-pkce-client',
    authorizationEndpoint: 'http://localhost:8181/realms/fitness-oauth2/protocol/openid-connect/auth',
    tokenEndpoint: 'http://localhost:8181/realms/fitness-oauth2/protocol/openid-connect/token',
    redirectUri: 'http://localhost:5174',
    scopes: 'openid profile email offline_access',
    onRefreshTokenExpired: (event) => {
        // Handle refresh token expiration
        event.logIn()
        // Optionally, redirect to login page or show a message
    }
};