import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, Bed, Users, FileText, Receipt, LineChart, LogOut } from 'lucide-react';
import { ThemeToggle } from '../components/ui/ThemeToggle.jsx';

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
            <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Admin Portal</h1>
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
          <Outlet />
        </div>
      </main>
    </div>
  );
}
