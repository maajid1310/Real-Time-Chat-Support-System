import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import ProtectedRoute from '../components/common/ProtectedRoute';

// Auth Pages
import Login from '../pages/Login/Login';
import Register from '../pages/Register/Register';

// Dashboard Pages (placeholders for now)
import AdminDashboard from '../pages/AdminDashboard/AdminDashboard';
import AgentDashboard from '../pages/AgentDashboard/AgentDashboard';
import CustomerDashboard from '../pages/CustomerDashboard/CustomerDashboard';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes - Admin */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          {/* Protected Routes - Agent */}
          <Route
            path="/agent/dashboard"
            element={
              <ProtectedRoute allowedRoles={['AGENT']}>
                <AgentDashboard />
              </ProtectedRoute>
            }
          />

          {/* Protected Routes - Customer */}
          <Route
            path="/customer/dashboard"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER']}>
                <CustomerDashboard />
              </ProtectedRoute>
            }
          />

          {/* Default Route */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          
          {/* 404 Route */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default AppRoutes;
