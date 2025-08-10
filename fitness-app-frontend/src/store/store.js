import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./authSlice";

export const store = configureStore({
  reducer: {
    // Add your reducers here
    // For example, if you have a counter slice:
    // counter: counterReducer,
    auth: authReducer,
  },
});