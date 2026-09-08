import React, { useEffect } from 'react';
import { X } from 'lucide-react';

const Modal = ({ isOpen, onClose, title, children, maxWidth = 'max-w-md', closeable = true }) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && closeable && onClose) onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose, closeable]);

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/50 backdrop-blur-xs transition-opacity overflow-y-auto"
      onClick={() => { if (closeable && onClose) onClose(); }}
    >
      <div
        className={`w-full ${maxWidth} max-h-[90vh] sm:max-h-[85vh] flex flex-col bg-white border border-slate-200 rounded-xl shadow-2xl overflow-hidden my-auto`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 sm:px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex-shrink-0">
          <h3 className="text-base font-semibold text-slate-900">{title}</h3>
          {closeable && onClose && (
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-slate-600 transition-colors p-1 rounded-md hover:bg-slate-100 cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>
        <div className="p-5 sm:p-6 overflow-y-auto flex-1">{children}</div>
      </div>
    </div>
  );
};

export default Modal;
