//
//  TCAppDelegate.m
//  FMOSXAPP
//
//  Created by Mario Rossi on 14/07/12.
//  Copyright (c) 2012 a. All rights reserved.
//


#import "TCAppDelegate.h"

@implementation TCAppDelegate
@synthesize myDescription = _myDescription;
@synthesize window = _window;
@synthesize theItem = _theItem;
@synthesize view = _view;
@synthesize searchBtn;
@synthesize searchLabel;
@synthesize chooseThisBtn;

- (void)dealloc
{
    [super dealloc];
}

//let's go on.
- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    NSLog(@"ok, go on"); //
    btDevice = nil;
    foundDevices = [[NSMutableArray alloc] init];
    ibdi = [IOBluetoothDeviceInquiry inquiryWithDelegate:self]; //inquiry, have delegates methoeds
    [ibdi setUpdateNewDeviceNames:YES]; //Yes, I want also names for the bt devices found.
    [ibdi retain];
   //  [ibdi start]; //start inquiry
    [self launchNotificationBar];
}

- (void) launchNotificationBar {
    NSStatusBar *bar = [NSStatusBar systemStatusBar];
    _theItem = [bar statusItemWithLength:NSVariableStatusItemLength];
    [_theItem retain];    
   // [theItem setTitle: NSLocalizedString(@"T.I.",@"")];
    [self showSetup];
    [_theItem setHighlightMode:YES];
    //[theItem setMenu:_menu];
}

- (void) show2G {
    [_theItem setImage:[NSImage imageNamed:@"2g.png"]];
}
- (void) show3G {
    [_theItem setImage:[NSImage imageNamed:@"3g.png"]];
}
- (void) showSetup {
    [_theItem setImage:[NSImage imageNamed:@"setup.png"]];
}
- (void) showNoRete {
    [_theItem setImage:[NSImage imageNamed:@"norete.png"]];
}

- (IBAction)searchAgain:(id)sender {
    [foundDevices removeAllObjects];
    [searchBtn setHidden:YES];
    [searchLabel setTitleWithMnemonic:@"Searching..."];
    [chooseThisBtn setHidden: YES];
    [ibdi start];
    [_view clearMe];
}

- (void) searchIsFinished{
    [searchBtn setHidden:NO];
    [chooseThisBtn setHidden: NO];
    [searchLabel setTitleWithMnemonic:@"Searching: complete"];
}

- (IBAction) chooseThisClicked:(id)sender {
    NSLog(@"Choose This Clicked");
    [self checkForService];
}


//---------------------------------------------------------------------------------------------------------------------------
/*!	@method			deviceInquiryStarted
 @discussion 	This message will be delivered when the inquiry actually starts. Since the inquiry could be throttled, this
 message may not be received immediately after called -start.
 @param			sender				Inquiry object that sent this delegate message.
 */

- (void)	deviceInquiryStarted:(IOBluetoothDeviceInquiry*)sender{
    NSLog(@"Let's start inquiry devices around me");
}

//---------------------------------------------------------------------------------------------------------------------------
/*!	@method			deviceInquiryDeviceFound
 @discussion 	A new device has been found. You do not need to retain the device - it will be held in the internal
 storage of the inquiry, and can be accessed later using -foundDevices.
 @param			sender				Inquiry object that sent this delegate message.
 @param			device				IOBluetoothDevice that was found.
 */

- (void)	deviceInquiryDeviceFound:(IOBluetoothDeviceInquiry*)sender	device:(IOBluetoothDevice*)device{
    NSLog(@"found this device %@ ", [device getAddressString]);
}

//---------------------------------------------------------------------------------------------------------------------------
/*!	@method			deviceInquiryUpdatingDeviceNamesStarted
 @discussion 	The inquiry has begun updating device names that were found during the search.
 @param			sender				Inquiry object that sent this delegate message.
 @param			devicesRemaining	Number of devices remaining to update.
 */

- (void)	deviceInquiryUpdatingDeviceNamesStarted:(IOBluetoothDeviceInquiry*)sender	devicesRemaining:(uint32_t)devicesRemaining{
    NSLog(@"updating names for: %d",devicesRemaining);
}

//---------------------------------------------------------------------------------------------------------------------------
/*!	@method			deviceInquiryDeviceNameUpdated
 @discussion 	A device name has been retrieved. Also indicates how many devices are left to be updated.
 @param			sender				Inquiry object that sent this delegate message.
 @param			device				IOBluetoothDevice that was updated.
 @param			devicesRemaining	Number of devices remaining to update.
 */

- (void)	deviceInquiryDeviceNameUpdated:(IOBluetoothDeviceInquiry*)sender	device:(IOBluetoothDevice*)device devicesRemaining:(uint32_t)devicesRemaining{
    NSString *btDevName = [device getName]; //this is a deprecated methoed.
    NSLog(@"New name for: %@",btDevName );
    
    [foundDevices addObject:device];
    [_view updateWith:[device getAddressString]:btDevName];
    /*if ([btDevName isEqualToString:@"Nexus S"]) {
    //if([btDevName isEqualToString:@"xperia"]) {
        NSLog(@"That's my boy! %@",btDevName);
        btDevice = device;
    }*/
        
}

//---------------------------------------------------------------------------------------------------------------------------
/*!	@method			deviceInquiryComplete
 @discussion 	When the inquiry is completely stopped, this delegate method will be invoked. It will supply an error
 code value, kIOReturnSuccess if the inquiry stopped without problem, otherwise a non-kIOReturnSuccess
 error code will be supplied.
 @param			sender				Inquiry object that sent this delegate message.
 @param			error				Error code. kIOReturnSuccess if the inquiry completed without incident.
 @param			aborted				TRUE if user called -stop on the inquiry.
 */

- (void)	deviceInquiryComplete:(IOBluetoothDeviceInquiry*)sender	error:(IOReturn)error	aborted:(BOOL)abort{
    [self searchIsFinished];

}

- (void) checkForService{
    for(int i=0;i<[foundDevices count];i++) {
        if([[[foundDevices objectAtIndex:i] getAddressString] isEqualToString:[_view selectedRow]]) {
            btDevice = [foundDevices objectAtIndex:i];
        }
    }
    if(btDevice != nil) { //so I have something to query.
        NSLog(@"I gottcha!");
        IOReturn ret = [btDevice performSDPQuery:self]; //some delegate methoed called after SDP is finished.
        if (ret != kIOReturnSuccess) {
            NSLog(@"smthg went wrong");
            return;
        }
        
        BluetoothRFCOMMChannelID rfCommChan;
        if([self findRfCommChannel:&rfCommChan] != kIOReturnSuccess) //check If the device have RFCOMM channel.
            return;
        
        NSLog(@"Found rfcomm channel on device.. %d",rfCommChan);
        [self openConnection:&rfCommChan];
    } else {
        
    }   
}

//called during SDP step.
- (void)sdpQueryComplete:(IOBluetoothDevice *)device status:(IOReturn)status {
    if (status != kIOReturnSuccess) {
        NSLog(@"SDP query got status %d", status);
        return;
    }
    NSLog(@"sdpCompleted");
}

//get the RfComm channel.
-(IOReturn) findRfCommChannel:(BluetoothRFCOMMChannelID *) rfChan{
    if(btDevice == nil)
        return kIOReturnNotFound;
    IOReturn ret;
    NSArray* services = [btDevice getServices];
    BluetoothRFCOMMChannelID newChan;
    for (IOBluetoothSDPServiceRecord* service in services) {
        NSString *serviceName = [service getServiceName];
        NSLog(@"Service: %@", serviceName);
        ret = [service getRFCOMMChannelID:&newChan];
        if (ret == kIOReturnSuccess && [serviceName isEqualToString:@"TETHERINGINDICATOR"]) {
            *rfChan = newChan;
            NSLog(@"ChannelID FOUND %d %d", newChan, *rfChan);
            return kIOReturnSuccess;
        } 
    }
    
    return kIOReturnNotFound;
} 

//Open the connection.
-(BOOL) openConnection:(BluetoothRFCOMMChannelID *) chanId{
    IOBluetoothRFCOMMChannel *channel;
    if ([btDevice openRFCOMMChannelAsync:&channel withChannelID:*chanId delegate:self] != kIOReturnSuccess) { // after connection it is established.. the delegates methoed are triggered.
        NSLog(@"Couldn't open channel");
        return NO;
    }

    [channel closeChannel];
    return YES;
}

//delegate RFComm channel
- (void)rfcommChannelOpenComplete:(IOBluetoothRFCOMMChannel*)rfcommChannel
                           status:(IOReturn)error {
    if (error != kIOReturnSuccess) {
        NSLog(@"Failed to open channel, error %d", error);
        return;
    }
    
    NSLog(@"channel complete");
}

//connection closed.
- (void)rfcommChannelClosed:(IOBluetoothRFCOMMChannel*)rfcommChannel {
    NSLog(@"Channel closed");
}

//reading data from rfcomm
- (void)rfcommChannelData:(IOBluetoothRFCOMMChannel*)rfcommChannel data:(void *)dataPointer length:(size_t)dataLength {
    NSString *newStr = [[NSString alloc] initWithBytes:dataPointer length:dataLength encoding:NSUTF8StringEncoding];
    NSLog(@"received: %@",newStr);
    NSMutableString *mut = [[NSMutableString alloc] init];
    for(int i = 0;i<[newStr length];i++){
        char c = [newStr characterAtIndex:i];
        if(c != ';') {
            [mut appendFormat:@"%c",c];
        } else {
            break;
        }
    }
    
    if([mut isEqualToString:@"Fast"]) {
        [self show3G];
    }
    
    if([mut isEqualToString:@"Slow"]) {
        [self show2G];
    }
    
    if([mut isEqualToString:@"Failed"]) {
        [self showNoRete];
    }
}

//writing data from rfcomm
- (void)rfcommChannelWriteComplete:(IOBluetoothRFCOMMChannel*)rfcommChannel refcon:(void*)refcon status:(IOReturn)error { /* not used yet  */}

@end
