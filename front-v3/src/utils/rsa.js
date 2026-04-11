import forge from 'node-forge'

export function encryptPasswordToHex ({ password, pubExpHex, pubModHex }) {
  const reversedPassword = String(password).split('').reverse().join('')
  const n = new forge.jsbn.BigInteger(String(pubModHex), 16)
  const e = new forge.jsbn.BigInteger(String(pubExpHex), 16)
  const publicKey = forge.pki.rsa.setPublicKey(n, e)
  const encryptedBytes = publicKey.encrypt(reversedPassword, 'RSAES-PKCS1-V1_5')
  return forge.util.bytesToHex(encryptedBytes).toUpperCase()
}
