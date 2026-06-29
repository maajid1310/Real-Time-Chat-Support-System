import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const AgentDashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ padding: '40px' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
          <h1>Agent Dashboard</h1>
          <button onClick={handleLogout} style={{ padding: '10px 20px', cursor: 'pointer' }}>
            Logout
          </button>
        </div>

        <div style={{ background: '#f5f5f5', padding: '20px', borderRadius: '8px' }}>
          <h3>Welcome, {user?.firstName} {user?.lastName}!</h3>
          <p>Email: {user?.email}</p>
          <p>Role: {user?.role}</p>
          <p>User ID: {user?.userId}</p>
        </div>

        <div style={{ marginTop: '30px' }}>
          <h3>Agent Features (Coming Soon)</h3>
          <ul>
            <li>Chat Management</li>
            <li>Customer Support</li>
            <li>Availability Status</li>
            <li>Chat History</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default AgentDashboard;
