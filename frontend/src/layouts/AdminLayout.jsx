import React, { useState, useEffect } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, Bed, Users, FileText, Receipt, LineChart, LogOut, Wifi, WifiOff, Loader2 } from 'lucide-react';
import { ThemeToggle } from '../components/ui/ThemeToggle.jsx';
import { get } from '../api.js';

// ── Backend Status Banner (MAIN BRANCH - Happy Path) ─────────────────
function BackendStatusBanner() {
  const [status, setStatus] = useState('checking');

  useEffect(() => {
    let isMounted = true;
    const check = async () => {
      try {
        await get('/rooms?q=&status=');
        if (isMounted) setStatus('online');
      } catch {
        if (isMounted) setStatus('offline');
      }
    };
    check();
    const interval = setInterval(check, 15000);
    return () => { isMounted = false; clearInterval(interval); };
  }, []);

  if (status === 'checking') {
    return (
      <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 mb-6 text-sm font-medium text-slate-500">
        <Loader2 size={18} className="animate-spin text-blue-500" />
        <span>Checking backend connection...</span>
      </div>
    );
  }

  if (status === 'online') {
    return (
      <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 mb-6 text-sm font-semibold text-green-700 dark:text-green-400">
        <Wifi size={18} />
        <span>Backend Connected — Spring Boot API is running on <code className="font-mono text-xs bg-green-100 dark:bg-green-900/40 px-1.5 py-0.5 rounded">localhost:8080</code></span>
        <span className="ml-auto text-green-600 text-xs font-normal">✓ Stable Branch — Main (Happy)</span>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 mb-6 text-sm font-semibold text-red-700 dark:text-red-400">
      <WifiOff size={18} />
      <span>Backend Offline — Cannot reach Spring Boot API on <code className="font-mono text-xs bg-red-100 dark:bg-red-900/40 px-1.5 py-0.5 rounded">localhost:8080</code></span>
      <span className="ml-auto text-red-500 text-xs font-normal">Please start the backend server.</span>
    </div>
  );
}

const navItems = [
  { path: '/admin', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/admin/rooms', label: 'Room Management', icon: Bed },
  { path: '/admin/tenants', label: 'Tenant Management', icon: Users },
  { path: '/admin/contracts', label: 'Contracts', icon: FileText },
  { path: '/admin/invoices', label: 'Billing & Finance', icon: Receipt },
  { path: '/admin/revenue', label: 'Revenue Stats', icon: LineChart },
];

export function AdminLayout({ user, onLogout }) {
  return (
    <div className="flex min-h-screen bg-slate-50 dark:bg-slate-950">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-950 text-slate-300 flex flex-col transition-all duration-300 shadow-xl z-10">
        <div className="p-6">
          <div className="text-2xl font-black text-white tracking-tight flex items-center gap-2">
            <span className="bg-gradient-to-br from-blue-500 to-teal-400 text-transparent bg-clip-text">RRMS</span>
            <small className="text-xs font-bold tracking-widest text-teal-400 mt-1">ADMIN</small>
          </div>
        </div>
        
        <nav className="flex-1 px-4 space-y-2 overflow-y-auto">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/admin'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-900/20'
                    : 'hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              <item.icon className="w-5 h-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 mt-auto">
          <button 
            onClick={onLogout}
            className="flex items-center gap-3 px-4 py-3 w-full text-left text-red-400 hover:bg-red-500/10 hover:text-red-300 rounded-xl transition-colors font-medium"
          >
            <LogOut className="w-5 h-5" />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="h-20 border-b border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900/50 backdrop-blur-sm flex items-center justify-between px-8 z-0">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Admin Portal <span className="text-green-500">(Happy)</span></h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Welcome back, {user?.username || 'Admin'}</p>
          </div>
          
          <div className="flex items-center gap-4">
            <ThemeToggle />
            <div className="h-8 w-px bg-border"></div>
            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-teal-100 text-teal-800 dark:bg-teal-900/30 dark:text-teal-400">
              Admin / Landlord
            </span>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-auto p-8">
          <BackendStatusBanner />
          <Outlet />
        </div>
      </main>
    </div>
  );
}
