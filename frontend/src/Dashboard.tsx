import React, { useState, useEffect } from 'react';

interface Order {
  id: number;
  amount: number;
  customer: string;
  status: string;
}

// PERF ISSUE: Component re-renders on every parent render (no React.memo)
const OrderRow = ({ order, onSelect }: { order: Order; onSelect: (id: number) => void }) => {
  // PERF ISSUE: Synchronous localStorage in render path
  const theme = localStorage.getItem('theme') || 'light';

  return (
    <tr className={theme}>
      <td>{order.id}</td>
      <td>{order.amount}</td>
      <td>{order.customer}</td>
      {/* PERF ISSUE: Inline arrow function creates new reference every render */}
      <td><button onClick={() => onSelect(order.id)}>Select</button></td>
    </tr>
  );
};

const Dashboard = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [filter, setFilter] = useState('');
  const [selected, setSelected] = useState<number[]>([]);

  // PERF ISSUE: Missing cleanup for event listener - memory leak
  useEffect(() => {
    window.addEventListener('resize', handleResize);
    const interval = setInterval(fetchOrders, 5000);
    // No cleanup function returned!
  }, []);

  // PERF ISSUE: Fetching inside useEffect without abort controller
  useEffect(() => {
    fetch('/api/orders')
      .then(res => res.json())
      .then(data => setOrders(data));
  }, [filter]); // Re-fetches on every filter change without debounce

  const handleResize = () => {
    // PERF ISSUE: DOM manipulation in loop
    const rows = document.querySelectorAll('tr');
    for (let i = 0; i < rows.length; i++) {
      const row = document.getElementById(`row-${i}`);
      if (row) {
        row.style.width = `${window.innerWidth - 40}px`;
        row.style.fontSize = window.innerWidth < 768 ? '12px' : '14px';
      }
    }
  };

  // PERF ISSUE: Expensive filter runs on every render, not memoized
  const filteredOrders = orders.filter(order => {
    return order.customer.toLowerCase().includes(filter.toLowerCase())
      && order.amount > 0
      && order.status !== 'cancelled';
  });

  // PERF ISSUE: Sorting on every render without useMemo
  const sortedOrders = [...filteredOrders].sort((a, b) => b.amount - a.amount);

  const fetchOrders = () => {
    fetch('/api/orders')
      .then(res => res.json())
      .then(data => setOrders(data));
  };

  return (
    <div>
      <input
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        placeholder="Filter..."
      />
      <table>
        <tbody>
          {sortedOrders.map(order => (
            <OrderRow
              key={order.id}
              order={order}
              // PERF ISSUE: Inline object prop causes re-render
              onSelect={(id) => setSelected([...selected, id])}
            />
          ))}
        </tbody>
      </table>
      {/* PERF ISSUE: Rendering large list without virtualization */}
      <div>
        {selected.map(id => (
          <span key={id} style={{ padding: '4px', margin: '2px', background: '#eee' }}>
            {id}
          </span>
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
