import picamera       
import time
import datetime
    
while True:
        with picamera.PiCamera() as camera:
                camera.resolution = (1280, 720)
                camera.rotation=-90
                #for i in range(1, 5):
                weekday=int(datetime.date.today().strftime("%w"))
                hour=datetime.datetime.now().hour
                if weekday > 0 or weekday<6:
                        print 'ttt %d  ' % weekday
                        if hour>7 or hour<17:
                                totalhour=17-hour
                                totalmin=60-datetime.datetime.now().minute
                                totalsecond=60-datetime.datetime.now().second
                                print 'hour %d  ' % hour
                                print 'min %d ' % datetime.datetime.now().minute
                                print 'cureent second %d ' % datetime.datetime.now().second
                                second=(totalhour*totalmin*60)-datetime.datetime.now().second
                                print 'sec %d' % second
                                camera.start_preview()
                                camera.start_recording('%d.h264' % second)
                                #print 'total time : %d ' % totalhour*totalmin*60
                                camera.wait_recording(second)
                                camera.stop_recording()
                                camera.stop_preview()
                        else:
                                print 'time out'
