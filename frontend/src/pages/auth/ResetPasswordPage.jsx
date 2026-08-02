import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth.service';
import { useNotification } from '../../context/NotificationContext';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import { Lock, ShieldCheck, CheckCircle2, Check, X } from 'lucide-react';

const ResetPasswordPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { showToast } = useNotification();

  const email = location.state?.email || '';
  const otpCode = location.state?.otpCode || '';

  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const password = formData.newPassword;
  const hasMinLength = password.length >= 8;
  const hasUppercase = /[A-Z]/.test(password);
  const hasLowercase = /[a-z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasNumber;

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.newPassword !== formData.confirmPassword) {
      setError('New password and confirm password do not match.');
      return;
    }

    if (!isPasswordValid) {
      setError('Password must be at least 8 characters long and include uppercase and lowercase letters and a number.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authService.resetPassword(
        email,
        otpCode,
        formData.newPassword,
        formData.confirmPassword
      );

      if (response.success) {
        setSuccess(true);
        showToast('Password Reset Successful. You can now log in.', 'success');
      } else {
        setError(response.message || 'Failed to reset password.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Password must be at least 8 characters long and include uppercase and lowercase letters and a number.';
      setError(msg);
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  if (!email || !otpCode) {
    return (
      <Card className="text-center py-8 px-6 shadow-md border-slate-200">
        <h3 className="text-lg font-bold text-slate-900 mb-2">Unauthorized Access</h3>
        <p className="text-xs text-slate-500 mb-6">
          Please complete the OTP verification step before resetting your password.
        </p>
        <Link to="/forgot-password">
          <Button variant="primary" className="w-full">
            Go to Forgot Password
          </Button>
        </Link>
      </Card>
    );
  }

  if (success) {
    return (
      <Card className="text-center py-8 px-6 shadow-md border-slate-200">
        <div className="mx-auto w-12 h-12 bg-emerald-50 border border-emerald-200 rounded-full flex items-center justify-center text-emerald-600 mb-4">
          <CheckCircle2 className="w-6 h-6" />
        </div>
        <h3 className="text-xl font-bold text-slate-900 mb-2">Password Reset Successful</h3>
        <p className="text-xs text-slate-600 leading-relaxed mb-6">
          Your password has been updated securely.
        </p>
        <Button variant="emerald" size="lg" className="w-full" onClick={() => navigate('/login')}>
          Sign In with New Password
        </Button>
      </Card>
    );
  }

  return (
    <Card className="shadow-md border-slate-200">
      <div className="mb-6 text-center">
        <h3 className="text-xl font-bold text-slate-900">Set New Password</h3>
        <p className="text-xs text-slate-500 mt-1">Creating new password for {email}</p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium leading-relaxed">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="New Password"
          type="password"
          name="newPassword"
          value={formData.newPassword}
          onChange={handleChange}
          placeholder="Enter new password"
          icon={Lock}
          required
        />

        <Input
          label="Confirm New Password"
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
          placeholder="Re-enter new password"
          icon={Lock}
          required
        />

        {password.length > 0 && (
          <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1.5">
            <p className="font-semibold text-slate-700 mb-1">Password Requirements:</p>
            <div className="grid grid-cols-2 gap-1.5 text-[11px]">
              <div className={`flex items-center gap-1.5 ${hasMinLength ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                {hasMinLength ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />}
                At least 8 characters
              </div>
              <div className={`flex items-center gap-1.5 ${hasUppercase ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                {hasUppercase ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />}
                One uppercase letter
              </div>
              <div className={`flex items-center gap-1.5 ${hasLowercase ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                {hasLowercase ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />}
                One lowercase letter
              </div>
              <div className={`flex items-center gap-1.5 ${hasNumber ? 'text-emerald-700 font-medium' : 'text-slate-500'}`}>
                {hasNumber ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />}
                One number
              </div>
            </div>
          </div>
        )}

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full mt-2"
          loading={loading}
          icon={ShieldCheck}
        >
          Update Password
        </Button>
      </form>
    </Card>
  );
};

export default ResetPasswordPage;
