import { CartProvider, useCart } from './context/CartContext';
import { CartSidebar } from './components/CartSidebar';
import { Checkout } from './components/Checkout';
import { OrderTracking } from './components/OrderTracking';
import { StoreContent } from './components/StoreContent';

function MainLayout() {
  const { view } = useCart();

  return (
    <>
      <div className="fixed inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-orange-900/20 via-stone-950 to-stone-950 -z-10 pointer-events-none" />
      <CartSidebar />

      {view === 'store' && <StoreContent />}
      {view === 'checkout' && <Checkout />}
      {view === 'tracking' && <OrderTracking />}
    </>
  );
}

function App() {
  return (
    <CartProvider>
      <MainLayout />
    </CartProvider>
  );
}

export default App;
