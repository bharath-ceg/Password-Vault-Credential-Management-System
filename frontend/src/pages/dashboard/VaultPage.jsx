import React, { useState, useEffect, useCallback } from 'react';
import { vaultService } from '../../services/vault.service';
import { useNotification } from '../../context/NotificationContext';
import Card from '../../components/ui/Card';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Spinner from '../../components/ui/Spinner';
import {
  Key,
  Search,
  Plus,
  Eye,
  Trash2,
  Edit,
  Globe,
  User,
  Copy,
  Check,
  ShieldAlert,
  ShieldCheck,
  Lock,
  Tag,
  X,
  KeyRound,
  Settings
} from 'lucide-react';

const CATEGORIES = [
  { id: 'ALL', label: 'All Items' },
  { id: 'SOCIAL_MEDIA', label: 'Social Media' },
  { id: 'BANKING', label: 'Banking' },
  { id: 'EMAIL', label: 'Email' },
  { id: 'SHOPPING', label: 'Shopping' },
  { id: 'DEVELOPER', label: 'Developer' },
  { id: 'OTHER', label: 'Other' },
];

const VaultPage = () => {
  const [credentials, setCredentials] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  // Privacy Password Status & First-Time Setup
  const [hasPrivacyPassword, setHasPrivacyPassword] = useState(true);
  const [isFirstTimeSetupOpen, setIsFirstTimeSetupOpen] = useState(false);
  const [setupForm, setSetupForm] = useState({ privacyPassword: '', confirmPrivacyPassword: '' });
  const [setupLoading, setSetupLoading] = useState(false);
  const [setupError, setSetupError] = useState('');

  // Change Privacy Password Modal
  const [isChangePrivacyOpen, setIsChangePrivacyOpen] = useState(false);
  const [changeForm, setChangeForm] = useState({
    loginPassword: '',
    newPrivacyPassword: '',
    confirmNewPrivacyPassword: ''
  });
  const [changeLoading, setChangeLoading] = useState(false);
  const [changeError, setChangeError] = useState('');

  // Add/Edit Credential Modal
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [formData, setFormData] = useState({
    applicationUrl: '',
    aliasName: '',
    username: '',
    password: '',
  });

  // Privacy Password Reveal Modal
  const [revealModalItem, setRevealModalItem] = useState(null);
  const [privacyPassword, setPrivacyPassword] = useState('');
  const [revealedPassword, setRevealedPassword] = useState('');
  const [revealLoading, setRevealLoading] = useState(false);
  const [revealError, setRevealError] = useState('');
  const [copiedId, setCopiedId] = useState(null);

  // Delete Confirmation Modal
  const [deleteModalItem, setDeleteModalItem] = useState(null);

  const { showToast } = useNotification();

  // Check Privacy Password Status
  const checkPrivacyStatus = useCallback(async () => {
    try {
      const response = await vaultService.getPrivacyPasswordStatus();
      if (response.success) {
        setHasPrivacyPassword(response.data);
        if (!response.data) {
          setIsFirstTimeSetupOpen(true);
        }
      }
    } catch (err) {
      console.error('Error checking privacy password status', err);
    }
  }, []);

  const fetchCredentials = useCallback(async () => {
    setLoading(true);
    try {
      let response;
      if (searchQuery.trim()) {
        response = await vaultService.searchCredentials(searchQuery);
      } else {
        const cat = selectedCategory === 'ALL' ? null : selectedCategory;
        response = await vaultService.getCredentials(cat);
      }
      if (response.success) {
        setCredentials(response.data || []);
      }
    } catch {
      showToast('Failed to load vault credentials.', 'error');
    } finally {
      setLoading(false);
    }
  }, [searchQuery, selectedCategory, showToast]);

  useEffect(() => {
    checkPrivacyStatus();
  }, [checkPrivacyStatus]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchCredentials();
    }, 300);
    return () => clearTimeout(timer);
  }, [fetchCredentials]);

  // First-Time Privacy Password Setup
  const handleSetupPrivacyPassword = async (e) => {
    e.preventDefault();

    if (setupForm.privacyPassword !== setupForm.confirmPrivacyPassword) {
      setSetupError('Privacy passwords do not match.');
      return;
    }

    const pass = setupForm.privacyPassword;
    const isValid = pass.length >= 8 && /[A-Z]/.test(pass) && /[a-z]/.test(pass) && /[0-9]/.test(pass) && /[@$!%*?&.#_\-]/.test(pass);
    if (!isValid) {
      setSetupError('Privacy password must contain at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.');
      return;
    }

    setSetupLoading(true);
    setSetupError('');

    try {
      const response = await vaultService.setPrivacyPassword(setupForm.privacyPassword);
      if (response.success) {
        showToast('Privacy Password set up successfully!', 'success');
        setHasPrivacyPassword(true);
        setIsFirstTimeSetupOpen(false);
      } else {
        setSetupError(response.message || 'Setup failed.');
      }
    } catch (err) {
      setSetupError(err.response?.data?.message || 'Failed to set up privacy password.');
    } finally {
      setSetupLoading(false);
    }
  };

  // Change Privacy Password Submit
  const handleChangePrivacyPassword = async (e) => {
    e.preventDefault();

    if (changeForm.newPrivacyPassword !== changeForm.confirmNewPrivacyPassword) {
      setChangeError('New Privacy Passwords do not match.');
      return;
    }

    const pass = changeForm.newPrivacyPassword;
    const isValid = pass.length >= 8 && /[A-Z]/.test(pass) && /[a-z]/.test(pass) && /[0-9]/.test(pass) && /[@$!%*?&.#_\-]/.test(pass);
    if (!isValid) {
      setChangeError('New privacy password must contain at least 8 characters, an uppercase letter, a lowercase letter, a number, and a special character.');
      return;
    }

    setChangeLoading(true);
    setChangeError('');

    try {
      const response = await vaultService.changePrivacyPassword({
        loginPassword: changeForm.loginPassword,
        newPrivacyPassword: changeForm.newPrivacyPassword
      });

      if (response.success) {
        showToast('Privacy password updated successfully.', 'success');
        setIsChangePrivacyOpen(false);
        setChangeForm({
          loginPassword: '',
          newPrivacyPassword: '',
          confirmNewPrivacyPassword: ''
        });
      }
    } catch (err) {
      setChangeError(err.response?.data?.message || 'Failed to update Privacy Password.');
    } finally {
      setChangeLoading(false);
    }
  };

  // Add / Edit Credential Form Handlers
  const handleOpenForm = (item = null) => {
    setFormError('');
    if (item) {
      setEditingItem(item);
      setFormData({
        applicationUrl: item.applicationUrl || '',
        aliasName: item.aliasName || '',
        username: item.username || '',
        password: '',
      });
    } else {
      setEditingItem(null);
      setFormData({ applicationUrl: '', aliasName: '', username: '', password: '' });
    }
    setIsFormOpen(true);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    const hasUrl = formData.applicationUrl && formData.applicationUrl.trim() !== '';
    const hasAlias = formData.aliasName && formData.aliasName.trim() !== '';

    if (!hasUrl && !hasAlias) {
      setFormError('At least one of Application URL or Alias Name must be provided.');
      return;
    }

    setFormLoading(true);

    try {
      if (editingItem) {
        const response = await vaultService.updateCredential(editingItem.id, formData);
        if (response.success) {
          showToast('Credential updated successfully!', 'success');
          setIsFormOpen(false);
          fetchCredentials();
        }
      } else {
        const response = await vaultService.createCredential(formData);
        if (response.success) {
          showToast('Credential saved to vault!', 'success');
          setIsFormOpen(false);
          fetchCredentials();
        }
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'At least one of Application URL or Alias Name must be provided.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteModalItem) return;
    try {
      const response = await vaultService.deleteCredential(deleteModalItem.id);
      if (response.success) {
        showToast('Credential permanently deleted.', 'success');
        setDeleteModalItem(null);
        fetchCredentials();
      }
    } catch {
      showToast('Failed to delete credential.', 'error');
    }
  };

  const handleRevealPassword = async (e) => {
    e.preventDefault();
    if (!revealModalItem) return;
    setRevealLoading(true);
    setRevealError('');

    try {
      const response = await vaultService.revealPassword(revealModalItem.id, privacyPassword);
      if (response.success && response.data) {
        setRevealedPassword(response.data);
        showToast('Password decrypted & revealed', 'success');
      }
    } catch (err) {
      const msg = err.response?.data?.message || 'Incorrect Privacy Password.';
      setRevealError(msg);
      showToast(msg, 'error');
    } finally {
      setRevealLoading(false);
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    showToast('Copied to clipboard!', 'info');
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getCategoryBadgeClass = (category) => {
    switch (category) {
      case 'SOCIAL_MEDIA': return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'BANKING': return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'EMAIL': return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'SHOPPING': return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'DEVELOPER': return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      default: return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  // Helper getters for setup modal
  const setupPass = setupForm.privacyPassword;
  const setupHasMin = setupPass.length >= 8;
  const setupHasUpper = /[A-Z]/.test(setupPass);
  const setupHasLower = /[a-z]/.test(setupPass);
  const setupHasNum = /[0-9]/.test(setupPass);
  const setupHasSpecial = /[@$!%*?&.#_\-]/.test(setupPass);

  return (
    <div className="space-y-6">
      {/* Header & Main Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <Key className="w-6 h-6 text-blue-600" /> Password Vault
          </h2>
          <p className="text-xs text-slate-500">
            Secure credential store
          </p>
        </div>
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
          <Button 
            variant="secondary" 
            icon={Settings} 
            onClick={() => {
              setChangeError('');
              setChangeForm({ loginPassword: '', currentPrivacyPassword: '', newPrivacyPassword: '', confirmNewPrivacyPassword: '' });
              setIsChangePrivacyOpen(true);
            }}
          >
            Change Privacy Password
          </Button>
          <Button variant="primary" icon={Plus} onClick={() => handleOpenForm()}>
            Add Credential
          </Button>
        </div>
      </div>

      {/* Search Bar & Category Filters */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by URL, alias, or username..."
            className="w-full bg-white border border-slate-200 rounded-lg pl-9 pr-4 py-2 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-xs"
          />
        </div>

        {/* Category Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 max-w-full">
          {CATEGORIES.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-colors border cursor-pointer flex-shrink-0 ${
                selectedCategory === cat.id
                  ? 'bg-blue-600 text-white border-blue-600 shadow-xs'
                  : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50 hover:text-slate-900'
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>
      </div>

      {/* Credentials Grid */}
      {loading ? (
        <div className="py-16 text-center">
          <Spinner size="lg" className="mx-auto text-blue-600" />
          <p className="text-xs text-slate-500 mt-3">Loading credentials...</p>
        </div>
      ) : credentials.length === 0 ? (
        <Card className="text-center py-16 px-4 border-slate-200">
          <div className="mx-auto w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mb-3">
            <Lock className="w-6 h-6" />
          </div>
          <h4 className="text-base font-bold text-slate-900">No Credentials Found</h4>
          <p className="text-xs text-slate-500 mt-1 mb-4">
            {searchQuery
              ? `No items match "${searchQuery}"`
              : 'Your password vault is empty. Click below to add an entry.'}
          </p>
          {!searchQuery && (
            <Button variant="primary" size="sm" icon={Plus} onClick={() => handleOpenForm()}>
              Add Credential
            </Button>
          )}
        </Card>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4 sm:gap-5">
          {credentials.map((item) => {
            const displayName = item.aliasName || item.applicationUrl || 'Credential';
            const displaySub = item.applicationUrl ? item.applicationUrl : item.aliasName;

            return (
              <Card key={item.id} hover className="flex flex-col justify-between space-y-4 border-slate-200">
                <div>
                  {/* Header Row */}
                  <div className="flex items-start justify-between gap-2 mb-3">
                    <div className="flex items-center gap-3 overflow-hidden">
                      <div className="w-9 h-9 rounded-lg bg-blue-50 border border-blue-100 flex items-center justify-center text-blue-700 font-bold text-sm flex-shrink-0">
                        {displayName.charAt(0).toUpperCase()}
                      </div>
                      <div className="overflow-hidden">
                        <h4 className="font-bold text-slate-900 text-sm truncate">{displayName}</h4>
                        {item.applicationUrl && (
                          <a
                            href={item.applicationUrl.startsWith('http') ? item.applicationUrl : `https://${item.applicationUrl}`}
                            target="_blank"
                            rel="noreferrer"
                            className="text-xs text-blue-600 hover:underline flex items-center gap-1 truncate"
                          >
                            <Globe className="w-3 h-3 flex-shrink-0" />
                            {item.applicationUrl}
                          </a>
                        )}
                      </div>
                    </div>
                    <span
                      className={`px-2 py-0.5 text-[10px] font-semibold tracking-wider rounded-md border uppercase whitespace-nowrap ${getCategoryBadgeClass(
                        item.category
                      )}`}
                    >
                      {item.category?.replace('_', ' ')}
                    </span>
                  </div>

                  {/* Username / Password Box */}
                  <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg space-y-2 text-xs">
                    <div className="flex items-center justify-between text-slate-700">
                      <span className="flex items-center gap-1.5 truncate">
                        <User className="w-3.5 h-3.5 text-slate-400" /> {item.username}
                      </span>
                      <button
                        onClick={() => copyToClipboard(item.username, `user-${item.id}`)}
                        className="text-slate-400 hover:text-slate-700 transition-colors p-1"
                        title="Copy Username"
                      >
                        {copiedId === `user-${item.id}` ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                      </button>
                    </div>

                    <div className="font-mono text-slate-600">
                      <span>{item.maskedPassword}</span>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  <Button
                    variant="secondary"
                    size="sm"
                    icon={Eye}
                    onClick={() => {
                      setRevealModalItem(item);
                      setPrivacyPassword('');
                      setRevealedPassword('');
                      setRevealError('');
                    }}
                  >
                    Reveal
                  </Button>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenForm(item)}
                      className="p-1.5 text-slate-500 hover:text-slate-900 rounded-md hover:bg-slate-100 transition-colors cursor-pointer"
                      title="Edit"
                    >
                      <Edit className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => setDeleteModalItem(item)}
                      className="p-1.5 text-slate-500 hover:text-red-600 rounded-md hover:bg-red-50 transition-colors cursor-pointer"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Modal 1: First-Time Privacy Password Setup */}
      <Modal
        isOpen={isFirstTimeSetupOpen}
        onClose={null}
        closeable={false}
        title="Set Up Your Privacy Password"
      >
        <form onSubmit={handleSetupPrivacyPassword} className="space-y-4">
          <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-900 leading-relaxed">
            🛡️ <strong>Welcome to Password Vault!</strong> Please configure a secondary Privacy Password to shield sensitive credential disclosures.
          </div>

          {setupError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {setupError}
            </div>
          )}

          <Input
            label="Privacy Password"
            type="password"
            value={setupForm.privacyPassword}
            onChange={(e) => {
              setSetupForm({ ...setupForm, privacyPassword: e.target.value });
              setSetupError('');
            }}
            placeholder="Enter privacy password"
            icon={Lock}
            required
          />

          <Input
            label="Confirm Privacy Password"
            type="password"
            value={setupForm.confirmPrivacyPassword}
            onChange={(e) => {
              setSetupForm({ ...setupForm, confirmPrivacyPassword: e.target.value });
              setSetupError('');
            }}
            placeholder="Re-enter privacy password"
            icon={Lock}
            required
          />

          {setupPass.length > 0 && (
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs space-y-1.5">
              <p className="font-semibold text-slate-700">Privacy Password Requirements:</p>
              <div className="grid grid-cols-2 gap-1.5 text-[11px]">
                <div className={`flex items-center gap-1.5 ${setupHasMin ? 'text-emerald-700' : 'text-slate-500'}`}>
                  {setupHasMin ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />} Min 8 chars
                </div>
                <div className={`flex items-center gap-1.5 ${setupHasUpper ? 'text-emerald-700' : 'text-slate-500'}`}>
                  {setupHasUpper ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />} Uppercase
                </div>
                <div className={`flex items-center gap-1.5 ${setupHasLower ? 'text-emerald-700' : 'text-slate-500'}`}>
                  {setupHasLower ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />} Lowercase
                </div>
                <div className={`flex items-center gap-1.5 ${setupHasNum ? 'text-emerald-700' : 'text-slate-500'}`}>
                  {setupHasNum ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />} Number
                </div>
                <div className={`flex items-center gap-1.5 col-span-2 ${setupHasSpecial ? 'text-emerald-700' : 'text-slate-500'}`}>
                  {setupHasSpecial ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <X className="w-3.5 h-3.5 text-slate-400" />} Special character (@$!%*?&.#_-)
                </div>
              </div>
            </div>
          )}

          <Button type="submit" variant="primary" className="w-full mt-2" loading={setupLoading} icon={ShieldCheck}>
            Save Privacy Password
          </Button>
        </form>
      </Modal>

      {/* Modal 2: Change Privacy Password */}
      <Modal
        isOpen={isChangePrivacyOpen}
        onClose={() => setIsChangePrivacyOpen(false)}
        title="Change Privacy Password"
      >
        <form onSubmit={handleChangePrivacyPassword} className="space-y-4">
          {changeError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {changeError}
            </div>
          )}

          <Input
            label="Account Login Password"
            type="password"
            value={changeForm.loginPassword}
            onChange={(e) => {
              setChangeForm({ ...changeForm, loginPassword: e.target.value });
              setChangeError('');
            }}
            placeholder="Enter account login password"
            icon={Lock}
            required
          />

          <Input
            label="New Privacy Password"
            type="password"
            value={changeForm.newPrivacyPassword}
            onChange={(e) => {
              setChangeForm({ ...changeForm, newPrivacyPassword: e.target.value });
              setChangeError('');
            }}
            placeholder="Enter new privacy password"
            icon={Lock}
            required
          />

          <Input
            label="Confirm New Privacy Password"
            type="password"
            value={changeForm.confirmNewPrivacyPassword}
            onChange={(e) => {
              setChangeForm({ ...changeForm, confirmNewPrivacyPassword: e.target.value });
              setChangeError('');
            }}
            placeholder="Re-enter new privacy password"
            icon={Lock}
            required
          />

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setIsChangePrivacyOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={changeLoading}>
              Update Privacy Password
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal 3: Add / Edit Credential */}
      <Modal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        title={editingItem ? 'Edit Vault Credential' : 'Add Credential'}
      >
        <form onSubmit={handleFormSubmit} className="space-y-4">
          {formError && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
              {formError}
            </div>
          )}

          <Input
            label="Application URL (Optional)"
            type="text"
            name="applicationUrl"
            value={formData.applicationUrl}
            onChange={(e) => {
              setFormData({ ...formData, applicationUrl: e.target.value });
              setFormError('');
            }}
            placeholder="Enter application URL (optional)"
            icon={Globe}
          />

          <Input
            label="Alias Name (Optional)"
            type="text"
            name="aliasName"
            value={formData.aliasName}
            onChange={(e) => {
              setFormData({ ...formData, aliasName: e.target.value });
              setFormError('');
            }}
            placeholder="Enter alias name (optional)"
            icon={Tag}
          />

          <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-lg text-[11px] text-slate-500">
            • At least one of Application URL or Alias Name must be provided.
          </div>

          <Input
            label="Username / Email"
            type="text"
            name="username"
            value={formData.username}
            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            placeholder="Enter username or email address"
            icon={User}
            required
          />

          <Input
            label="Password"
            type="password"
            name="password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            placeholder={editingItem ? 'Enter new password to update' : 'Enter password'}
            icon={Lock}
            required={!editingItem}
          />

          <div className="pt-2 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={formLoading}>
              {editingItem ? 'Save Changes' : 'Save Credential'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal 4: Reveal Password */}
      <Modal
        isOpen={!!revealModalItem}
        onClose={() => {
          setRevealModalItem(null);
          setRevealedPassword('');
          setRevealError('');
        }}
        title={`Reveal Password for ${revealModalItem?.aliasName || revealModalItem?.applicationUrl || 'Credential'}`}
      >
        {revealedPassword ? (
          <div className="space-y-4 text-center py-2">
            <p className="text-xs text-slate-500">Decrypted Secret Password:</p>
            <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg flex items-center justify-between font-mono text-lg text-emerald-800 font-bold">
              <span>{revealedPassword}</span>
              <button
                onClick={() => copyToClipboard(revealedPassword, 'revealed')}
                className="text-emerald-700 hover:text-emerald-900 p-2 cursor-pointer"
                title="Copy Password"
              >
                {copiedId === 'revealed' ? <Check className="w-5 h-5 text-emerald-600" /> : <Copy className="w-5 h-5" />}
              </button>
            </div>
            <Button
              variant="secondary"
              className="w-full mt-4"
              onClick={() => {
                setRevealModalItem(null);
                setRevealedPassword('');
                setRevealError('');
              }}
            >
              Close
            </Button>
          </div>
        ) : (
          <form onSubmit={handleRevealPassword} className="space-y-4">
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-start gap-2.5 text-xs text-amber-900">
              <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
              <span>
                Enter your Privacy Password to decrypt and reveal the stored credential.
              </span>
            </div>

            {revealError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-xs font-medium">
                {revealError}
              </div>
            )}

            <Input
              label="Privacy Password"
              type="password"
              value={privacyPassword}
              onChange={(e) => {
                setPrivacyPassword(e.target.value);
                setRevealError('');
              }}
              placeholder="Enter your privacy password"
              icon={Lock}
              required
            />

            <div className="pt-2 flex justify-end gap-3">
              <Button 
                variant="secondary" 
                onClick={() => {
                  setRevealModalItem(null);
                  setRevealError('');
                }}
              >
                Cancel
              </Button>
              <Button type="submit" variant="emerald" loading={revealLoading}>
                Decrypt Password
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {/* Modal 5: Delete Confirmation */}
      <Modal
        isOpen={!!deleteModalItem}
        onClose={() => setDeleteModalItem(null)}
        title="Delete Credential"
      >
        <div className="space-y-4">
          <p className="text-sm text-slate-600 leading-relaxed">
            Are you sure you want to permanently delete <strong className="text-slate-900">{deleteModalItem?.aliasName || deleteModalItem?.applicationUrl}</strong>? This action cannot be undone.
          </p>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" onClick={() => setDeleteModalItem(null)}>
              Cancel
            </Button>
            <Button variant="danger" icon={Trash2} onClick={handleDelete}>
              Delete Permanently
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default VaultPage;
