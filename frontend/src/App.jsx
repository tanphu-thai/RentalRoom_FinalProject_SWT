import React, { useState, createContext, useContext } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext.jsx';
import { post } from './api.js';
import { Alert } from './components/ui/Alert.jsx';

// Layouts
import { AdminLayout } from './layouts/AdminLayout.jsx';
import { TenantLayout } from './layouts/TenantLayout.jsx';

// Pages
import { LoginScreen } from './pages/Login.jsx';
import { TenantPortal } from './pages/TenantPortal.jsx';
import { Dashboard, RoomsPage, TenantsPage, ContractsPage, InvoicesPage, RevenuePage } from './pages/AdminPages.jsx';

export const NotifyContext = createContext(null);
export function useNotify() { return useContext(NotifyContext); }

// Global notification state wrapper
function GlobalNotifier({ children }) {
  const [notice, setNotice] = useState(null);
  const notify = (message, type = 'success') => {
    setNotice({ message, type });
    setTimeout(() => setNotice(null), 5000);
  };
  
  return (
    <NotifyContext.Provider value={notify}>
      <div className="fixed top-4 right-4 z-[9999] w-full max-w-sm pointer-events-none transition-all duration-300">
        <div className="pointer-events-auto">
          <Alert notice={notice} onClose={() => setNotice(null)} />
        </div>
      </div>
      {children}
    </NotifyContext.Provider>
  );
}

function AppRoutes() {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('rrms_token');
    return token ? { token, role: localStorage.getItem('rrms_role'), username: localStorage.getItem('rrms_username') } : null;
  });

  const logout = async () => {
    try {
      if (user?.token) await post('/auth/logout', {});
    } catch (_) {} finally {
      localStorage.clear();
      setUser(null);
      window.location.href = '/';
    }
  };

  if (!user) {
    return <LoginScreen onLogin={setUser} />;
  }

  if (user.role === 'TENANT') {
    return (
      <GlobalNotifier>
        <Routes>
          <Route element={<TenantLayout user={user} onLogout={logout} />}>
            <Route path="*" element={<TenantPortal />} />
          </Route>
        </Routes>
      </GlobalNotifier>
    );
  }

  return (
    <GlobalNotifier>
      <Routes>
        <Route element={<AdminLayout user={user} onLogout={logout} />}>
          <Route path="/admin" element={<Dashboard />} />
          <Route path="/admin/rooms" element={<RoomsPage />} />
          <Route path="/admin/tenants" element={<TenantsPage />} />
          <Route path="/admin/contracts" element={<ContractsPage />} />
          <Route path="/admin/invoices" element={<InvoicesPage />} />
          <Route path="/admin/revenue" element={<RevenuePage />} />
          <Route path="*" element={<Navigate to="/admin" replace />} />
        </Route>
      </Routes>
    </GlobalNotifier>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </ThemeProvider>
  );
}
