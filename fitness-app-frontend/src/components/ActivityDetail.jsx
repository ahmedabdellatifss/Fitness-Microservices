import React, { use } from 'react';
import { getActivitiesDetail } from '../services/api';
import { Box, Divider, Typography } from '@mui/material';    
import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Card, CardContent } from '@mui/material';
import { useNavigate } from 'react-router-dom';




const ActivityDetail = () => {

  const {id} = useParams();
  const [activity, setActivity] = useState(null);
  const [recommendations, setRecommendations] = useState(null);

  useEffect(() => {
    fetchRecommendations();
  },[id]);

  const fetchActivity = async () => {
    try {
      const response = await getActivitiesDetail(id);
      setRecommendations(response.data);
    } catch (error) {
      console.error('Error fetching activity:', error);
    }
  };
  if(!activity) {
    return <Typography>Loading activity details...</Typography>;
  }
  return (
    <Box sx={{ p: 2, maxWidth: 800, mx: 'auto' }}>
      <Card sx={{cursor: 'pointer'}} onClick={() => navigate(`/activities/${activity.id}`)}>
          <CardContent> 
            <Typography variant="h5" gutterBottom>Activity Detials</Typography>
            <Typography variant="h5" >{activity.type}</Typography>
            <Typography>Duration: {activity.duration} minutes</Typography>
            <Typography>Calories Burned: {activity.caloriesBurned}</Typography>
            <Typography>Data: {new Data(activity.createdAt).toLocaleString()}</Typography>       
          </CardContent>
        </Card>

        {recommendations && (

          <Card sx={{cursor: 'pointer'}} >
            <CardContent> 
              <Typography variant="h5" gutterBottom>AI Recommendation</Typography>
              <Typography variant="h5" >Analysis</Typography>
              <Typography>Duration: {activity.recommendations}</Typography>

              <Divider sx={{ my: 2 }} />

              <Typography variant='h2'>Improvements</Typography>
              {activity?.improvements?.map((improvement, index) => (
                <Typography key={index}>{improvement}</Typography>
              ))}

              <Divider sx={{ my: 2 }} />

              <Typography variant='h2'>Suggestion</Typography>
              {activity?.suggestions?.map((suggestion, index) => (
                <Typography key={index}>{suggestion}</Typography>
              ))}       
            </CardContent>
          </Card>
        )}
    </Box>  
  )
}

export default ActivityDetail
