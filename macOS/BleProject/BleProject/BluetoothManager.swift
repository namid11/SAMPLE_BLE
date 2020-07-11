import Foundation
import CoreBluetooth

// MACはCentral（データを利用する側）です。一応クライアント？
// スマホはPeripheral（データを渡す側）。一応サーバ？

class BluetoothManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    var cbCentralManager: CBCentralManager!
    var targetPeripheral: CBPeripheral? = nil
    var notifyCharacteristic: CBCharacteristic? = nil
    var targetDescriptor: CBDescriptor? = nil
    let serviceUUID = [CBUUID(string: "0783266c-3115-4a24-8ace-b61baed1ea3a")]
    let notifyCharacreristicUUID = CBUUID(string: "5ac98f44-c344-11ea-87d0-0242ac130003")
    let cccdUUID = CBUUID(string: CBUUIDClientCharacteristicConfigurationString)
    
    override init () {
        super.init()
        cbCentralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOff:
            print("POWERED OFF")
            break
        case .poweredOn:
            print("POWERED ON")
            break
        case .resetting:
            print("RESETTING")
            break
        case .unauthorized:
            print("UN Authorized")
            break
        case .unknown:
            print("unknown")
            break
        case .unsupported:
            print("not supported")
            break
        default:
            print("?")
        }
    }

    func scan() {
        if (!cbCentralManager.isScanning) {
            print("start scan")
            cbCentralManager.scanForPeripherals(withServices: serviceUUID, options: nil)  // データを渡す周辺機器（ペリフェラル）を探索。見つかったらcentralManager(didDiscover)が呼び出される
        } else {
            print("doing scan...")
        }

    }
    
    // call this when manager found peripheral devices
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print("found peripheral devices")
        print("next, it try to connecting to peripheral device...")
        self.targetPeripheral = peripheral
        self.cbCentralManager.connect(self.targetPeripheral!, options: nil)
        cbCentralManager.stopScan()
    }
    
    
    // call this when self connect to peripheral device
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("succeeded connecting")
        print("next, it try to discovering service...")
        targetPeripheral?.delegate = self  // サーバから何かある度にイベントを受け取れるようにデリゲートをセット
        targetPeripheral?.discoverServices(serviceUUID)
    }
    
    // call this when it discovered services
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if error != nil {
            print("failed discovering services")
            print(error!)
            return
        }
        
        print("found any services")
        print("service count: ", peripheral.services?.count ?? 0)
        for service in peripheral.services ?? [] {
            if(service.uuid.uuidString == serviceUUID[0].uuidString) {
                targetPeripheral?.discoverCharacteristics(nil, for: service)
             }
        }
        print("next, it try to discovering characteristics...")
    }
    
    
    // call this when it discovered characteristics
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("found any characteristics")
        print("characteristic count:", service.characteristics!.count)
        for characreristic in service.characteristics!{
            if characreristic.uuid == notifyCharacreristicUUID {
                print("exist notify characteristic")
                self.notifyCharacteristic = characreristic
                self.targetPeripheral?.discoverDescriptors(for: characreristic)
            }
        }
        print("next, it try to discovering descriptors...")
    }
    
    // call this when it discovered descriptors
    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        print("found any descriptors")
        for descriptor in characteristic.descriptors ?? [] {
            if descriptor.uuid == cccdUUID {
                print("exist cccd")
                self.targetDescriptor = descriptor
            }
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("Disconnect")
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("connection failed")
    }
    
    func peripheralIsReady(toSendWriteWithoutResponse peripheral: CBPeripheral) {
        print("Send Writing")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if error != nil {
            print("failed writing process")
            print(error!)
            return
        }
        
        print("write value", characteristic.value ?? "NULL")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let e = error {
            print("Error: on update value", e)
            return
        }
        
        if let data = characteristic.value {
            print("Characteristic data:", String(data: data, encoding: .utf8) ?? "NONE")
        } else {
            print("Characteristic data: NONE")
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if let e = error {
            print("Error: on update notification state for")
            print(e)
            return
        }

        print("OK: Update enable notification state")
    }
    
    
    
    func readCharacreristic() {
        let thread = Thread.init {
            if let c = self.notifyCharacteristic {
                self.targetPeripheral?.readValue(for: c)
            } else {
                
            }
        }
        thread.start()
    }

    func write() {
        self.targetPeripheral?.writeValue("ABC".data(using: .utf8) ?? Data(), for: notifyCharacteristic!, type: .withResponse)
    }
    
    func setNotify() {
        self.targetPeripheral?.setNotifyValue(true, for: notifyCharacteristic!)
    }
}
