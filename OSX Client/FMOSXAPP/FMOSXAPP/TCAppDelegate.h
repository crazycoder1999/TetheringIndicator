//
//  TCAppDelegate.h
//  FMOSXAPP
//
//  Created by Mario Rossi on 14/07/12.
//  Copyright (c) 2012 a. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <IOBluetooth/IOBluetooth.h>
#import "TIViewController.h"

@interface TCAppDelegate : NSObject <NSApplicationDelegate,NSTableViewDataSource,NSTableViewDelegate>
{
    IOBluetoothDevice* btDevice;
    IOBluetoothDeviceInquiry *ibdi;
    NSMutableArray *foundDevices;
}

@property (assign) IBOutlet NSTextField *searchLabel;
@property (assign) IBOutlet NSButton *searchBtn;
@property (assign) IBOutlet NSButton *chooseThisBtn;
@property (assign)  IBOutlet TIViewController *view; 
@property (strong) NSStatusItem *theItem;
@property (assign) IBOutlet NSWindow *window;
@property (assign) IBOutlet NSTextField *myDescription;

- (void) checkForService;
- (IBAction)chooseThisClicked:(id)sender;
- (IBAction)searchAgain:(id)sender;
- (void) searchIsFinished;
- (void) launchNotificationBar;
- (void) show3G;
- (void) show2G;
- (void) showNoRete;
- (void) showSetup;
-(BOOL) openConnection:(BluetoothRFCOMMChannelID *) chanId;
-(IOReturn) findRfCommChannel:(BluetoothRFCOMMChannelID *) rfChan;
@end
