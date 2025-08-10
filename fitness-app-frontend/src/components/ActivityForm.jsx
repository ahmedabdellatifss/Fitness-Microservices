import React from 'react'
import { Box, Button, InputLabel, FormControl, MenuItem, Select, TextField } from '@mui/material' 
import { addActivity } from '../services/api';



const ActivityForm = ({ onActivityAdded }) => {

    const [activity, setActivity] = React.useState({
        type: "RUNNING",
        duration: '',
        caloriesBurned: '',
        additionalMetrics: {}
    });

    const handelSubmit = async (event) => {
        event.preventDefault();
        try{
            await addActivity(activity);
            onActivityAdded();
            setActivity( {type: "RUNNING", duration: '', caloriesBurned: '' });
        }catch (error) {
            console.error("Error adding activity:", error);
        }
    }  

  return (
    <div>
        <Box component="form" onSubmit={handelSubmit} sx={{ mb: 4 }}>
            <FormControl fullWidth sx={{mb: 2}}>
                <InputLabel id="activity-type-label">Activity Type</InputLabel>
                <Select 
                    value={activity.type}
                    onChange={(e) => setActivity({ ...activity, type: e.target.value })}
                >
                    <MenuItem value="RUNNING">Running</MenuItem>
                    <MenuItem value="WALKING">WALKING</MenuItem>
                    <MenuItem value="SWIMMING">Swimming</MenuItem>
                </Select>
            </FormControl>
            <TextField 
                label="Duration (minutes)"
                type="number"
                value={activity.duration}
                onChange={(e) => setActivity({ ...activity, duration: e.target.value })}
                fullWidth
                sx={{ mb: 2 }}
            />
            <TextField 
                label="Calories Burned"
                type="number"
                value={activity.caloriesBurned}
                onChange={(e) => setActivity({ ...activity, caloriesBurned: e.target.value })}
                fullWidth
                sx={{ mb: 2 }}
            />
            <Button type='submit' variant="contained" color="primary">
                Add Activity
            </Button>  
        </Box>
    </div>
  )
}

export default ActivityForm
