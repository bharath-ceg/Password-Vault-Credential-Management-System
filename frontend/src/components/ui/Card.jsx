import React from 'react';

const Card = ({ children, className = '', hover = false, ...props }) => {
  return (
    <div
      className={`bg-white border border-slate-200 rounded-xl p-6 shadow-sm transition-all duration-150 ${
        hover ? 'hover:border-slate-300 hover:shadow-md' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default Card;
