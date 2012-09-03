//
//  TIViewController.h
//  FMOSXAPP
//
//  Created by Mario Rossi on 26/08/12.
//  Copyright (c) 2012 a. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TIViewController : NSViewController<NSTableViewDataSource,NSTableViewDelegate> {
    //ho bisogno di un array... per i mac address e uno per name.
    NSMutableArray *names;
    NSMutableArray *macs;
}

@property (assign) IBOutlet NSTableView *myTable;
- (void) updateWith:(NSString *) mac:(NSString *) name;
- (void) clearMe;
- (NSString *) selectedRow;
@end
