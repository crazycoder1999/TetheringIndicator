//
//  TIViewController.m
//  FMOSXAPP
//
//  Created by Mario Rossi on 26/08/12.
//  Copyright (c) 2012 a. All rights reserved.
//

#import "TIViewController.h"

@implementation TIViewController
@synthesize myTable;

- (void) clearMe{
    [names removeAllObjects];
    [macs removeAllObjects];
}

- (NSString *) selectedRow{
    if([myTable selectedRow] != -1)
        return [macs objectAtIndex:[myTable selectedRow]];
    return @"";
}

- (void) updateWith:(NSString *)mac:(NSString *)name {
    NSLog(@"Update %@ number %d",name,(int)[names count]);
    [names addObject:name];
    [macs addObject:mac];
    [myTable reloadData];
}

- (void)awakeFromNib{
    NSLog(@"Awake!");
    names = [[NSMutableArray alloc] init];
    macs = [[NSMutableArray alloc] init];
}

- (NSInteger)numberOfRowsInTableView:(NSTableView *)tableView {
    return [names count];
}

- (id)tableView:(NSTableView *)tableView objectValueForTableColumn:(NSTableColumn *)tableColumn row:(NSInteger)row {
    return nil;
}

- (void)tableView:(NSTableView *)tableView willDisplayCell:(id)cell forTableColumn:(NSTableColumn *)tableColumn row:(NSInteger)row {
    NSString *strCell = nil;
    if([[tableColumn identifier] isEqualTo:@"macaddress"]) {
        strCell = [[NSString alloc] initWithFormat:@"%@",[macs objectAtIndex:row]];
    } else if ([[tableColumn identifier] isEqualTo:@"btname"]) {
        strCell = [[NSString alloc] initWithFormat:@"%@",[names objectAtIndex:row]];
    }
    if(strCell != nil) {
        [strCell retain];
        [cell setTitle:strCell];
        [cell setWraps:YES];
        [cell setEditable:NO];
    }
}

- (CGFloat)tableView:(NSTableView *)tableView heightOfRow:(NSInteger)row AVAILABLE_MAC_OS_X_VERSION_10_4_AND_LATER {
    return 20.0;
}

@end
