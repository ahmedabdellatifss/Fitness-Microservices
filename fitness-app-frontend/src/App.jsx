import React, { useContext, useEffect, useState } from 'react'
import { Route, BrowserRouter as Router, Routes } from 'react-router'
import './App.css'
import { Button } from '@mui/material'
import { Box } from '@mui/material'
import { AuthContext } from 'react-oauth2-code-pkce'
import { useDispatch } from 'react-redux'
import { setCredentials } from './store/authSlice'
import ActivityForm from './components/ActivityForm'
import ActivityList from './components/ActivityList'
import ActivityDetail from './components/ActivityDetail'
import { Navigate } from 'react-router'

const ActivitiesPage = () => (
  <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
    <ActivityForm onActivitiesAdded={() => window.location.reload()} />
    <ActivityList />
  </Box>
);

function App() {
  const { token, tokenData, logIn } = useContext(AuthContext);
  const dispatch = useDispatch();
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    if(token){
      dispatch(setCredentials({
        user: tokenData,
        token: token,
        userId: tokenData.sub
      }));
      setAuthReady(true);
    }
  },[token, tokenData, dispatch])

  if (!authReady) {
    return <div>Loading authentication...</div>;
  }
  return (
    <Router>
      {!token ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '2rem' }}>
          <div style={{ marginBottom: '1rem' }}>You are not logged in.</div>
          <Button 
            variant="contained"
            color="secondary"
            onClick={logIn}
          >
            LOGIN
          </Button>
        </div>
      ) : (
        <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/activities/:id" element={<ActivityDetail />} />
            <Route path="/" element={token ? <Navigate to="/activities" replace/> : <div> Wellcome! please login</div>} />
          </Routes>  
        </Box>
      )}
    </Router>
  );
}

export default App
