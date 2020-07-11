//
//  ViewController.swift
//  BleProject
//
//  Created by NH on 2020/07/11.
//  Copyright © 2020 NH. All rights reserved.
//

import Cocoa

class ViewController: NSViewController {

    let bm = BluetoothManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override var representedObject: Any? {
        didSet {
        // Update the view, if already loaded.
        }
    }

    @IBAction func scan(_ sender: Any) {
        self.bm.scan()
    }
    
    @IBAction func setNotify(_ sender: Any) {
        self.bm.setNotify()
    }
    
    @IBAction func readCharecteristic(_ sender: Any) {
        self.bm.readCharacreristic()
    }
    
    @IBAction func writeCharacteristic(_ sender: Any) {
        self.bm.write()
    }
}

