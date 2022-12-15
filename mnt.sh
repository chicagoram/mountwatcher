sleep 30 
umount -f /mnt/41/Towers/$2 > /dev/null 2>&1 ||: 
mount -t cifs //$1/Jobs /mnt/41/Towers/$2 -o username=Administrator,password=p0tt3r,iocharset=utf8,file_mode=0777,dir_mode=0777
