import base64
import zlib

import rsa
from Crypto.Cipher import AES
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import algorithms


class RsaEncrypt(object):
    def __init__(self):
        self.m = 'D70D7EA6DCF57CE38B0E84CFBF585921D9405872CC035FA7B725D9AF025CA37B823904C5FEA63C179278BE6A4E87E3F424EA930DEFC09FBDFC46EDB7684252CC66005CB3F4EC84F8A094DDC687F867657A1E140EE58AD98CC5DE4134535F5351CC57ACDBB407A848E75D10CDAE2D0F69B854985327788F6BA5E96A75E047E9F1'
        self.e = '10001'

    def encrypt(self, message):
        mm = int(self.m, 16)
        ee = int(self.e, 16)
        rsa_pubkey = rsa.PublicKey(mm, ee)
        crypto = self._encrypt(message, rsa_pubkey)
        return crypto

    def _pad_for_encryption(self, message, target_length):
        msglength = len(message)
        padding = b''
        padding_length = target_length - msglength - 3
        for i in range(padding_length):
            padding += b'\xff'
        return b''.join([b'\x00\x02', padding, b'\x00', message])

    def _encrypt(self, message, pub_key):
        keylength = rsa.common.byte_size(pub_key.n)
        padded = self._pad_for_encryption(message, keylength)
        payload = rsa.transform.bytes2int(padded)
        encrypted = rsa.core.encrypt_int(payload, pub_key.e, pub_key.n)
        block = rsa.transform.int2bytes(encrypted, keylength)
        return block


class AesCbcEncrypt(object):
    def __init__(self, key, iv):
        self.key = key
        self.mode = AES.MODE_CBC
        self.iv = iv

    def encrypt(self, text):
        cryptor = AES.new(self.key, self.mode, self.iv)
        text = self.pkcs7_padding(text)
        return cryptor.encrypt(text)

    @staticmethod
    def pkcs7_padding(data):
        if not isinstance(data, bytes):
            data = data.encode()
        padder = padding.PKCS7(algorithms.AES.block_size).padder()
        padded_data = padder.update(data) + padder.finalize()
        return padded_data


if __name__ == "__main__":
    arg1 = "http://app.weixin.sogou.com/api/searchapp"
    arg2 = "type=2&ie=utf8&page=1&query=%E5%A5%8B%E9%A3%9E%E5%AE%89%E5%85%A8&select_count=1&tsn=1&usip="
    arg3 = "lilac"
    v10 = "5231a01e6146841cbaef0134dcd9300d9fce87072d0dc6789817468974b2ea51ee3944b8d7e0a88e4f16ebb80f03bd84"
    v5 = "4573636f77446f7269734361726c6f73924b035fbda56ae5ef0ed05a08de7aecc8abe1835e0c4a548f7803937f4c3b45"
    key, iv = bytes.fromhex(v10[32:]), bytes.fromhex(v10[:32])

    v35 = base64.b64encode(RsaEncrypt().encrypt(
        bytes.fromhex(v10[32:]))).decode()
    v36 = base64.b64encode(bytes.fromhex(v10[:32])).decode()
    v14 = base64.b64encode(bytes.fromhex("".join([
        hex(int(v10[32:][i-1] + v10[32:][i], 16) ^
            int(v5[32:][i-1] + v5[32:][i], 16))[2:].zfill(2)
        for i in range(1, len(v10[32:]), 2)]))).decode()
    v32 = base64.b64encode(AesCbcEncrypt(key, iv).encrypt(
        bytes.fromhex(zlib.compress(arg1.encode()).hex()[4:-8]))).decode()
    v34 = base64.b64encode(AesCbcEncrypt(key, iv).encrypt(
        bytes.fromhex(zlib.compress(arg2.encode()).hex()[4:-8]))).decode()
    v41 = base64.b64encode(AesCbcEncrypt(key, iv).encrypt(
        bytes.fromhex(zlib.compress(arg3.encode()).hex()[4:-8]))).decode()
    print(f"k={v35}&v={v36}&u={v32}&r={v14}&g={v34}&p={v41}")
