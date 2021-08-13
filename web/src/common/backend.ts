import AxiosStatic, { AxiosResponse } from 'axios';
import auth from './auth';

const axios = AxiosStatic.create({
  baseURL: import.meta.env.DEV ? 'http://localhost:9090' : '',
  headers: {
    'Content-Type': 'application/json'
  }
});
axios.interceptors.request.use(async request => {
  const token = await auth.bearerToken();
  request.headers['Authorization'] = 'Bearer ' + token;
  return request;
});

export class VaultDto {
  constructor(public name: string, public masterkey: string, public iterations: number, public salt: string) { }
}
class VaultService {

  public async get(vaultId: string): Promise<AxiosResponse<VaultDto>> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    return axios.get(`/vaults/${vaultId}`);
  }

  public async createVault(vaultId: string, name: string, masterkey: string, iterations: number, salt: string): Promise<AxiosResponse<any>> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    const body: VaultDto = { name: name, masterkey: masterkey, iterations: iterations, salt: salt };
    return axios.put(`/vaults/${vaultId}`, body);
  }

  public async getKeyFor(vaultId: string, deviceId: String): Promise<AxiosResponse<AccessDto>> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    return axios.get(`/vaults/${vaultId}/keys/${deviceId}`);
  }

  public async grantAccess(vaultId: string, deviceId: string, deviceSpecificMasterkey: string, ephemeralPublicKey: string) {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    const body: AccessDto = { device_specific_masterkey: deviceSpecificMasterkey, ephemeral_public_key: ephemeralPublicKey }
    await axios.put(`/vaults/${vaultId}/keys/${deviceId}`, body);
  }

  public async revokeAccess(vaultId: string, deviceId: string) {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    await axios.delete(`/vaults/${vaultId}/keys/${deviceId}`);
  }
}

export class AccessDto {
  constructor(public device_specific_masterkey: string, public ephemeral_public_key: string) { }
}

class DeviceService {

  public async createDevice(deviceId: string, name: string, publicKey: String): Promise<AxiosResponse<any>> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    const body = { id: deviceId, name: name, publicKey: publicKey }
    return axios.put(`/devices/${deviceId}`, body)
  }

  public async getDevice(deviceId: string): Promise<AxiosResponse<DeviceDto>> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    return axios.get<DeviceDto>(`/devices/${deviceId}`)
  }

  public async listAll(): Promise<DeviceDto[]> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    return axios.get<DeviceDto[]>('/devices/').then(response => response.data)
  }

}

export class DeviceDto {
  constructor(public id: string, public name: string, public publicKey: string) { }
}

class UserService {
  public async me(): Promise<string> {
    if (!auth.isAuthenticated()) {
      return Promise.reject('not logged in');
    }
    return axios.get<string>('/users/me').then(response => response.data)
  }
}

const services = {
  vaults: new VaultService(),
  users: new UserService(),
  devices: new DeviceService()
};

export default services;