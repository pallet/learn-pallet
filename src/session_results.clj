(defsection session-results)
(ns session-results
  (:require [pallet.actions :as actions]
            [pallet.api :as api]))

;; Running a lift or a converge operation returns a session object.
;; The this session contains all the information relevant for the
;; operation, and pallet.repl has functions to inspect the session,
;; and more importantly, to know what happened with it.

;; We have defined two groups `good-group` and `bad-group`, each with
;; two phases with trivial actions. The difference between the two
;; groups is that the second phase 

(def good-group
  (api/group-spec
      "good"
    :extends *base-spec*
    :phases
    {:first (api/plan-fn
             (actions/exec-script
              (println "first!")))
     :second (api/plan-fn
              (actions/exec-checked-script
               "say hello!"
               ("echo" "hello world!")))}))

(def bad-group
  (api/group-spec
      "bad"
    :extends *base-spec*
    :phases
    {:first (api/plan-fn
             (actions/exec-script (println "first!")))
     :second (api/plan-fn
              (actions/exec-script
               (println "hello world!"))
              (actions/exec-checked-script
               "fail!"
               ("exit -1")))}))

(defn run
  ([] (run 2 2))
  ([n] (run n 2))
  ([n m]
     (api/converge {good-group n
                    bad-group m}
                   :compute *compute*
                   :phase [:first :second])))

(defn destroy
  (run 0 0))

(comment
  (def s (run))
  (session-summary s)
  ;; nodes created: 4
  ;; PHASES: bootstrap, first, second
  ;; GROUPS: bad, good
  ;; ACTIONS:
  ;;   PHASE bootstrap:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.108: OK
  ;;       NODE 192.168.56.110: OK
  ;;     GROUP good:
  ;;       NODE 192.168.56.109: OK
  ;;       NODE 192.168.56.111: OK
  ;;   PHASE first:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.110: OK
  ;;       NODE 192.168.56.108: OK
  ;;     GROUP good:
  ;;       NODE 192.168.56.111: OK
  ;;       NODE 192.168.56.109: OK
  ;;   PHASE second:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.108: ERROR
  ;;       NODE 192.168.56.110: ERROR
  ;;     GROUP good:
  ;;       NODE 192.168.56.109: OK
  ;;       NODE 192.168.56.111: OK

  (explain-session s)
  ;;   session-results> (explain-session s)
  ;; nodes created: 4
  ;; PHASES: bootstrap, first, second
  ;; GROUPS: bad, good
  ;; ACTIONS:
  ;;   PHASE bootstrap:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.108:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'package-manager update ...';
  ;;           | {
  ;;           | apt-get -qq update
  ;;           |  } || { echo '#> package-manager update  : FAIL'; exit 1;} >&2 
  ;;           | echo '#> package-manager update  : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: package-manager update ...
  ;;           | #> package-manager update  : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user: install]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo '[automated-admin-user: install]: Packages...';
  ;;           | {
  ;;           | { debconf-set-selections <<EOF
  ;;           | debconf debconf/frontend select noninteractive
  ;;           | debconf debconf/frontend seen false
  ;;           | EOF
  ;;           | } && enableStart() {
  ;;           | rm /usr/sbin/policy-rc.d
  ;;           | } && apt-get -q -y install sudo+ && dpkg --get-selections
  ;;           |  } || { echo '#> [automated-admin-user: install]: Packages : FAIL'; exit 1;} >&2 
  ;;           | echo '#> [automated-admin-user: install]: Packages : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: [automated-admin-user: install]: Packages...
  ;;           | Reading package lists...
  ;;           | Building dependency tree...
  ;;           | Reading state information...
  ;;           | The following packages will be upgraded:
  ;;           |   sudo
  ;;           | 1 upgraded, 0 newly installed, 0 to remove and 163 not upgraded.
  ;;           | Need to get 288 kB of archives.
  ;;           | After this operation, 16.4 kB disk space will be freed.
  ;;           | Get:1 http://us.archive.ubuntu.com/ubuntu/ precise-updates/main sudo amd64 1.8.3p1-1ubuntu3.4 [288 kB]
  ;;           | Fetched 288 kB in 21s (13.4 kB/s)
  ;;           | (Reading database ... 
  ;;           | (Reading database ... 5%
  ;;           | (Reading database ... 10%
  ;;           | (Reading database ... 15%
  ;;           | (Reading database ... 20%
  ;;           | (Reading database ... 25%
  ;;           | (Reading database ... 30%
  ;;           | (Reading database ... 35%
  ;;           | (Reading database ... 40%
  ;;           | (Reading database ... 45%
  ;;           | (Reading database ... 50%
  ;;           | (Reading database ... 55%
  ;;           | (Reading database ... 60%
  ;;           | (Reading database ... 65%
  ;;           | (Reading database ... 70%
  ;;           | (Reading database ... 75%
  ;;           | (Reading database ... 80%
  ;;           | (Reading database ... 85%
  ;;           | (Reading database ... 90%
  ;;           | (Reading database ... 95%
  ;;           | (Reading database ... 100%
  ;;           | (Reading database ... 53234 files and directories currently installed.)
  ;;           | Preparing to replace sudo 1.8.3p1-1ubuntu3.2 (using .../sudo_1.8.3p1-1ubuntu3.4_amd64.deb) ...
  ;;           | Unpacking replacement sudo ...
  ;;           | Processing triggers for ureadahead ...
  ;;           | ureadahead will be reprofiled on next reboot
  ;;           | Processing triggers for man-db ...
  ;;           | Setting up sudo (1.8.3p1-1ubuntu3.4) ...
  ;;           | Installing new version of config file /etc/pam.d/sudo ...
  ;;           | accountsservice					install
  ;;           | acpid						install
  ;;           | adduser						install
  ;;           | apparmor					install
  ;;           | apport						install
  ;;           | apport-symptoms					install
  ;;           | apt						install
  ;;           | apt-transport-https				install
  ;;           | apt-utils					install
  ;;           | apt-xapian-index				install
  ;;           | aptitude					install
  ;;           | at						install
  ;;           | base-files					install
  ;;           | base-passwd					install
  ;;           | bash						install
  ;;           | bash-completion					install
  ;;           | bc						install
  ;;           | bind9-host					install
  ;;           | binutils					install
  ;;           | bsdmainutils					install
  ;;           | bsdutils					install
  ;;           | build-essential					install
  ;;           | busybox-initramfs				install
  ;;           | busybox-static					install
  ;;           | byobu						install
  ;;           | bzip2						install
  ;;           | ca-certificates					install
  ;;           | command-not-found				install
  ;;           | command-not-found-data				install
  ;;           | console-setup					install
  ;;           | coreutils					install
  ;;           | cpio						install
  ;;           | cpp						install
  ;;           | cpp-4.6						install
  ;;           | crda						install
  ;;           | cron						install
  ;;           | curl						install
  ;;           | dash						install
  ;;           | dbus						install
  ;;           | debconf						install
  ;;           | debconf-i18n					install
  ;;           | debianutils					install
  ;;           | diffutils					install
  ;;           | dmidecode					install
  ;;           | dmsetup						install
  ;;           | dnsutils					install
  ;;           | dosfstools					install
  ;;           | dpkg						install
  ;;           | dpkg-dev					install
  ;;           | e2fslibs					install
  ;;           | e2fsprogs					install
  ;;           | ed						install
  ;;           | eject						install
  ;;           | fakeroot					install
  ;;           | file						install
  ;;           | findutils					install
  ;;           | fonts-ubuntu-font-family-console		install
  ;;           | friendly-recovery				install
  ;;           | ftp						install
  ;;           | fuse						install
  ;;           | g++						install
  ;;           | g++-4.6						install
  ;;           | gcc						install
  ;;           | gcc-4.6						install
  ;;           | gcc-4.6-base					install
  ;;           | geoip-database					install
  ;;           | gettext-base					install
  ;;           | gir1.2-glib-2.0					install
  ;;           | gnupg						install
  ;;           | gpgv						install
  ;;           | grep						install
  ;;           | groff-base					install
  ;;           | grub-common					install
  ;;           | grub-gfxpayload-lists				install
  ;;           | grub-pc						install
  ;;           | grub-pc-bin					install
  ;;           | grub2-common					install
  ;;           | gzip						install
  ;;           | hdparm						install
  ;;           | hostname					install
  ;;           | ifupdown					install
  ;;           | info						install
  ;;           | initramfs-tools					install
  ;;           | initramfs-tools-bin				install
  ;;           | initscripts					install
  ;;           | insserv						install
  ;;           | install-info					install
  ;;           | installation-report				install
  ;;           | iproute						install
  ;;           | iptables					install
  ;;           | iputils-ping					install
  ;;           | iputils-tracepath				install
  ;;           | irqbalance					install
  ;;           | isc-dhcp-client					install
  ;;           | isc-dhcp-common					install
  ;;           | iso-codes					install
  ;;           | kbd						install
  ;;           | keyboard-configuration				install
  ;;           | klibc-utils					install
  ;;           | krb5-locales					install
  ;;           | landscape-common				install
  ;;           | language-pack-en				install
  ;;           | language-pack-en-base				install
  ;;           | language-selector-common			install
  ;;           | laptop-detect					install
  ;;           | less						install
  ;;           | libaccountsservice0				install
  ;;           | libacl1						install
  ;;           | libalgorithm-diff-perl				install
  ;;           | libalgorithm-diff-xs-perl			install
  ;;           | libalgorithm-merge-perl				install
  ;;           | libapt-inst1.4					install
  ;;           | libapt-pkg4.12					install
  ;;           | libasn1-8-heimdal				install
  ;;           | libattr1					install
  ;;           | libbind9-80					install
  ;;           | libblkid1					install
  ;;           | libboost-iostreams1.46.1			install
  ;;           | libbsd0						install
  ;;           | libbz2-1.0					install
  ;;           | libc-bin					install
  ;;           | libc-dev-bin					install
  ;;           | libc6						install
  ;;           | libc6-dev					install
  ;;           | libcap-ng0					install
  ;;           | libclass-accessor-perl				install
  ;;           | libclass-isa-perl				install
  ;;           | libcomerr2					install
  ;;           | libcurl3					install
  ;;           | libcurl3-gnutls					install
  ;;           | libcwidget3					install
  ;;           | libdb5.1					install
  ;;           | libdbus-1-3					install
  ;;           | libdbus-glib-1-2				install
  ;;           | libdevmapper1.02.1				install
  ;;           | libdns81					install
  ;;           | libdpkg-perl					install
  ;;           | libdrm-intel1					install
  ;;           | libdrm-nouveau1a				install
  ;;           | libdrm-radeon1					install
  ;;           | libdrm2						install
  ;;           | libedit2					install
  ;;           | libelf1						install
  ;;           | libept1.4.12					install
  ;;           | libevent-2.0-5					install
  ;;           | libexpat1					install
  ;;           | libffi6						install
  ;;           | libfreetype6					install
  ;;           | libfribidi0					install
  ;;           | libfuse2					install
  ;;           | libgc1c2					install
  ;;           | libgcc1						install
  ;;           | libgcrypt11					install
  ;;           | libgdbm3					install
  ;;           | libgeoip1					install
  ;;           | libgirepository-1.0-1				install
  ;;           | libglib2.0-0					install
  ;;           | libgmp10					install
  ;;           | libgnutls26					install
  ;;           | libgomp1					install
  ;;           | libgpg-error0					install
  ;;           | libgpm2						install
  ;;           | libgssapi-krb5-2				install
  ;;           | libgssapi3-heimdal				install
  ;;           | libhcrypto4-heimdal				install
  ;;           | libheimbase1-heimdal				install
  ;;           | libheimntlm0-heimdal				install
  ;;           | libhx509-5-heimdal				install
  ;;           | libidn11					install
  ;;           | libio-string-perl				install
  ;;           | libisc83					install
  ;;           | libisccc80					install
  ;;           | libisccfg82					install
  ;;           | libiw30						install
  ;;           | libjs-jquery					install
  ;;           | libk5crypto3					install
  ;;           | libkeyutils1					install
  ;;           | libklibc					install
  ;;           | libkrb5-26-heimdal				install
  ;;           | libkrb5-3					install
  ;;           | libkrb5support0					install
  ;;           | libldap-2.4-2					install
  ;;           | liblocale-gettext-perl				install
  ;;           | liblockfile-bin					install
  ;;           | liblockfile1					install
  ;;           | liblwres80					install
  ;;           | liblzma5					install
  ;;           | libmagic1					install
  ;;           | libmount1					install
  ;;           | libmpc2						install
  ;;           | libmpfr4					install
  ;;           | libncurses5					install
  ;;           | libncursesw5					install
  ;;           | libnewt0.52					install
  ;;           | libnfnetlink0					install
  ;;           | libnih-dbus1					install
  ;;           | libnih1						install
  ;;           | libnl-3-200					install
  ;;           | libnl-genl-3-200				install
  ;;           | libp11-kit0					install
  ;;           | libpam-modules					install
  ;;           | libpam-modules-bin				install
  ;;           | libpam-runtime					install
  ;;           | libpam0g					install
  ;;           | libparse-debianchangelog-perl			install
  ;;           | libparted0debian1				install
  ;;           | libpcap0.8					install
  ;;           | libpci3						install
  ;;           | libpciaccess0					install
  ;;           | libpcre3					install
  ;;           | libpcsclite1					install
  ;;           | libpipeline1					install
  ;;           | libplymouth2					install
  ;;           | libpng12-0					install
  ;;           | libpolkit-gobject-1-0				install
  ;;           | libpopt0					install
  ;;           | libpython2.7					install
  ;;           | libquadmath0					install
  ;;           | libreadline6					install
  ;;           | libroken18-heimdal				install
  ;;           | librtmp0					install
  ;;           | libsasl2-2					install
  ;;           | libsasl2-modules				install
  ;;           | libselinux1					install
  ;;           | libsigc++-2.0-0c2a				install
  ;;           | libslang2					install
  ;;           | libsqlite3-0					install
  ;;           | libss2						install
  ;;           | libssl1.0.0					install
  ;;           | libstdc++6					install
  ;;           | libstdc++6-4.6-dev				install
  ;;           | libsub-name-perl				install
  ;;           | libswitch-perl					install
  ;;           | libtasn1-3					install
  ;;           | libtext-charwidth-perl				install
  ;;           | libtext-iconv-perl				install
  ;;           | libtext-wrapi18n-perl				install
  ;;           | libtimedate-perl				install
  ;;           | libtinfo5					install
  ;;           | libudev0					install
  ;;           | libusb-0.1-4					install
  ;;           | libusb-1.0-0					install
  ;;           | libuuid1					install
  ;;           | libwind0-heimdal				install
  ;;           | libwrap0					install
  ;;           | libx11-6					install
  ;;           | libx11-data					install
  ;;           | libxapian22					install
  ;;           | libxau6						install
  ;;           | libxcb1						install
  ;;           | libxdmcp6					install
  ;;           | libxext6					install
  ;;           | libxml2						install
  ;;           | libxmuu1					install
  ;;           | linux-firmware					install
  ;;           | linux-headers-3.2.0-23				install
  ;;           | linux-headers-3.2.0-23-generic			install
  ;;           | linux-headers-server				install
  ;;           | linux-image-3.2.0-23-generic			install
  ;;           | linux-image-server				install
  ;;           | linux-libc-dev					install
  ;;           | linux-server					install
  ;;           | locales						install
  ;;           | lockfile-progs					install
  ;;           | login						install
  ;;           | logrotate					install
  ;;           | lsb-base					install
  ;;           | lsb-release					install
  ;;           | lshw						install
  ;;           | lsof						install
  ;;           | ltrace						install
  ;;           | make						install
  ;;           | makedev						install
  ;;           | man-db						install
  ;;           | manpages					install
  ;;           | manpages-dev					install
  ;;           | mawk						install
  ;;           | memtest86+					install
  ;;           | mime-support					install
  ;;           | mlocate						install
  ;;           | module-assistant				install
  ;;           | module-init-tools				install
  ;;           | mount						install
  ;;           | mountall					install
  ;;           | mtr-tiny					install
  ;;           | multiarch-support				install
  ;;           | nano						install
  ;;           | ncurses-base					install
  ;;           | ncurses-bin					install
  ;;           | net-tools					install
  ;;           | netbase						install
  ;;           | netcat-openbsd					install
  ;;           | ntfs-3g						install
  ;;           | ntpdate						install
  ;;           | openssh-client					install
  ;;           | openssh-server					install
  ;;           | openssl						install
  ;;           | os-prober					install
  ;;           | parted						install
  ;;           | passwd						install
  ;;           | patch						install
  ;;           | pciutils					install
  ;;           | perl						install
  ;;           | perl-base					install
  ;;           | perl-modules					install
  ;;           | plymouth					install
  ;;           | plymouth-theme-ubuntu-text			install
  ;;           | popularity-contest				install
  ;;           | powermgmt-base					install
  ;;           | ppp						install
  ;;           | pppconfig					install
  ;;           | pppoeconf					install
  ;;           | procps						install
  ;;           | psmisc						install
  ;;           | python						install
  ;;           | python-apport					install
  ;;           | python-apt					install
  ;;           | python-apt-common				install
  ;;           | python-chardet					install
  ;;           | python-crypto					install
  ;;           | python-dbus					install
  ;;           | python-dbus-dev					install
  ;;           | python-debian					install
  ;;           | python-gdbm					install
  ;;           | python-gi					install
  ;;           | python-gnupginterface				install
  ;;           | python-httplib2					install
  ;;           | python-keyring					install
  ;;           | python-launchpadlib				install
  ;;           | python-lazr.restfulclient			install
  ;;           | python-lazr.uri					install
  ;;           | python-minimal					install
  ;;           | python-newt					install
  ;;           | python-oauth					install
  ;;           | python-openssl					install
  ;;           | python-pam					install
  ;;           | python-pkg-resources				install
  ;;           | python-problem-report				install
  ;;           | python-serial					install
  ;;           | python-simplejson				install
  ;;           | python-twisted-bin				install
  ;;           | python-twisted-core				install
  ;;           | python-wadllib					install
  ;;           | python-xapian					install
  ;;           | python-zope.interface				install
  ;;           | python2.7					install
  ;;           | python2.7-minimal				install
  ;;           | readline-common					install
  ;;           | resolvconf					install
  ;;           | rsync						install
  ;;           | rsyslog						install
  ;;           | screen						install
  ;;           | sed						install
  ;;           | sensible-utils					install
  ;;           | sgml-base					install
  ;;           | ssh-import-id					install
  ;;           | strace						install
  ;;           | sudo						install
  ;;           | sysv-rc						install
  ;;           | sysvinit-utils					install
  ;;           | tar						install
  ;;           | tasksel						install
  ;;           | tasksel-data					install
  ;;           | tcpd						install
  ;;           | tcpdump						install
  ;;           | telnet						install
  ;;           | time						install
  ;;           | tmux						install
  ;;           | tzdata						install
  ;;           | ubuntu-keyring					install
  ;;           | ubuntu-minimal					install
  ;;           | ubuntu-standard					install
  ;;           | ucf						install
  ;;           | udev						install
  ;;           | ufw						install
  ;;           | update-manager-core				install
  ;;           | update-notifier-common				install
  ;;           | upstart						install
  ;;           | ureadahead					install
  ;;           | usbutils					install
  ;;           | util-linux					install
  ;;           | uuid-runtime					install
  ;;           | vim						install
  ;;           | vim-common					install
  ;;           | vim-runtime					install
  ;;           | vim-tiny					install
  ;;           | w3m						install
  ;;           | wget						install
  ;;           | whiptail					install
  ;;           | whoopsie					install
  ;;           | wireless-regdb					install
  ;;           | wireless-tools					install
  ;;           | wpasupplicant					install
  ;;           | xauth						install
  ;;           | xkb-data					install
  ;;           | xml-core					install
  ;;           | xz-lzma						install
  ;;           | xz-utils					install
  ;;           | zlib1g						install
  ;;           | #> [automated-admin-user: install]: Packages : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | if getent passwd tbatchelli; then /usr/sbin/usermod --shell "/bin/bash" tbatchelli;else /usr/sbin/useradd --shell "/bin/bash" --create-home tbatchelli;fi
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: 
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...';
  ;;           | {
  ;;           | mkdir -m "755" -p $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chown --recursive tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chmod 755 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...';
  ;;           | {
  ;;           | touch $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chown tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chmod 644 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...';
  ;;           | {
  ;;           | auth_file=$(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && if ! ( fgrep "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local" ${auth_file} ); then
  ;;           | echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local
  ;;           | " >> ${auth_file}
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...';
  ;;           | {
  ;;           | if hash chcon 2>&- && [ -d /etc/selinux ] && [ -e /selinux/enforce ] && stat --format %C $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ 2>&-; then chcon -Rv --type=user_home_t $(getent passwd tbatchelli | cut -d: -f6)/.ssh/;fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: sudoers: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: sudoers: remote-file /etc/sudoers...';
  ;;           | {
  ;;           | filediff= && if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | filediff=$?
  ;;           | fi && md5diff= && if [ -e /var/lib/pallet/etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers.md5 ]; then
  ;;           | ( cd $(dirname /var/lib/pallet/etc/sudoers.md5) && md5sum --quiet --check $(basename /var/lib/pallet/etc/sudoers.md5) )
  ;;           | md5diff=$?
  ;;           | fi && errexit=0 && if [ "${filediff}" == "1" ]; then
  ;;           | echo Existing file did not match the pallet master copy: FAIL
  ;;           | errexit=1
  ;;           | fi && if [ "${md5diff}" == "1" ]; then
  ;;           | echo Existing content did not match md5: FAIL
  ;;           | errexit=1
  ;;           | fi && [ "${errexit}" == "0" ] && mkdir -p $(dirname /tmp/root/bBCdbpTJf7UZAHwfBogFgg) && { cat > /tmp/root/bBCdbpTJf7UZAHwfBogFgg <<EOFpallet
  ;;           | Defaults env_keep=SSH_AUTH_SOCK
  ;;           | root ALL = (ALL) ALL
  ;;           | %adm ALL = (ALL) ALL
  ;;           | tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | EOFpallet
  ;;           |  } && contentdiff= && if [ -e /etc/sudoers ] && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | diff -u /etc/sudoers /tmp/root/bBCdbpTJf7UZAHwfBogFgg
  ;;           | contentdiff=$?
  ;;           | fi && if ! { [ "${contentdiff}" == "0" ]; } && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | chown root /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chgrp $(id -ng root) /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chmod 0440 /tmp/root/bBCdbpTJf7UZAHwfBogFgg && mv -f /tmp/root/bBCdbpTJf7UZAHwfBogFgg /etc/sudoers && dirpath=$(dirname /var/lib/pallet/etc/sudoers)
  ;;           | templatepath=$(dirname $(if [ -e /etc/sudoers ]; then readlink -f /etc/sudoers;else echo /etc/sudoers;fi))
  ;;           | if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi
  ;;           | templatepath=$(readlink -f ${templatepath})
  ;;           | mkdir -p ${dirpath} || exit 1
  ;;           | while [ "/" != "${templatepath}" ] ;do d=${dirpath} && t=${templatepath} && if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi && dirpath=$(dirname ${dirpath}) && templatepath=$(dirname ${templatepath}) && chgrp $(stat -c%G ${t}) ${d} || : && chmod $(stat -c%a ${t}) ${d} || : && chown $(stat -c%U ${t}) ${d} || : ; done && contentdiff=
  ;;           | if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | contentdiff=$?
  ;;           | fi
  ;;           | if ! { [ "${contentdiff}" == "0" ]; } && [ -e /etc/sudoers ]; then cp -f --backup="numbered" /etc/sudoers /var/lib/pallet/etc/sudoers;fi && ls -t /var/lib/pallet/etc/sudoers.~[0-9]*~ 2> /dev/null | tail -n "+6" | xargs \
  ;;           |  rm --force && (cp=$(readlink -f /etc/sudoers) && cd $(dirname ${cp}) && md5sum $(basename ${cp})
  ;;           | )>/var/lib/pallet/etc/sudoers.md5 && echo MD5 sum is $(cat /var/lib/pallet/etc/sudoers.md5)
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: sudoers: remote-file /etc/sudoers...
  ;;           | --- /etc/sudoers	2012-01-31 10:56:42.000000000 -0500
  ;;           | +++ /tmp/root/bBCdbpTJf7UZAHwfBogFgg	2014-01-16 14:29:42.411669422 -0500
  ;;           | @@ -1,29 +1,4 @@
  ;;           | -#
  ;;           | -# This file MUST be edited with the 'visudo' command as root.
  ;;           | -#
  ;;           | -# Please consider adding local content in /etc/sudoers.d/ instead of
  ;;           | -# directly modifying this file.
  ;;           | -#
  ;;           | -# See the man page for details on how to write a sudoers file.
  ;;           | -#
  ;;           | -Defaults	env_reset
  ;;           | -Defaults	secure_path="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
  ;;           | -
  ;;           | -# Host alias specification
  ;;           | -
  ;;           | -# User alias specification
  ;;           | -
  ;;           | -# Cmnd alias specification
  ;;           | -
  ;;           | -# User privilege specification
  ;;           | -root	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# Members of the admin group may gain root privileges
  ;;           | -%admin ALL=(ALL) ALL
  ;;           | -
  ;;           | -# Allow members of group sudo to execute any command
  ;;           | -%sudo	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# See sudoers(5) for more information on "#include" directives:
  ;;           | -
  ;;           | -#includedir /etc/sudoers.d
  ;;           | +Defaults env_keep=SSH_AUTH_SOCK
  ;;           | +root ALL = (ALL) ALL
  ;;           | +%adm ALL = (ALL) ALL
  ;;           | +tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | MD5 sum is 7c74ebd65015e958c87276681e97de4b sudoers
  ;;           | #> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS
  ;;       NODE 192.168.56.110:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'package-manager update ...';
  ;;           | {
  ;;           | apt-get -qq update
  ;;           |  } || { echo '#> package-manager update  : FAIL'; exit 1;} >&2 
  ;;           | echo '#> package-manager update  : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: package-manager update ...
  ;;           | #> package-manager update  : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user: install]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo '[automated-admin-user: install]: Packages...';
  ;;           | {
  ;;           | { debconf-set-selections <<EOF
  ;;           | debconf debconf/frontend select noninteractive
  ;;           | debconf debconf/frontend seen false
  ;;           | EOF
  ;;           | } && enableStart() {
  ;;           | rm /usr/sbin/policy-rc.d
  ;;           | } && apt-get -q -y install sudo+ && dpkg --get-selections
  ;;           |  } || { echo '#> [automated-admin-user: install]: Packages : FAIL'; exit 1;} >&2 
  ;;           | echo '#> [automated-admin-user: install]: Packages : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: [automated-admin-user: install]: Packages...
  ;;           | Reading package lists...
  ;;           | Building dependency tree...
  ;;           | Reading state information...
  ;;           | The following packages will be upgraded:
  ;;           |   sudo
  ;;           | 1 upgraded, 0 newly installed, 0 to remove and 163 not upgraded.
  ;;           | Need to get 288 kB of archives.
  ;;           | After this operation, 16.4 kB disk space will be freed.
  ;;           | Get:1 http://us.archive.ubuntu.com/ubuntu/ precise-updates/main sudo amd64 1.8.3p1-1ubuntu3.4 [288 kB]
  ;;           | Fetched 288 kB in 2s (128 kB/s)
  ;;           | (Reading database ... 
  ;;           | (Reading database ... 5%
  ;;           | (Reading database ... 10%
  ;;           | (Reading database ... 15%
  ;;           | (Reading database ... 20%
  ;;           | (Reading database ... 25%
  ;;           | (Reading database ... 30%
  ;;           | (Reading database ... 35%
  ;;           | (Reading database ... 40%
  ;;           | (Reading database ... 45%
  ;;           | (Reading database ... 50%
  ;;           | (Reading database ... 55%
  ;;           | (Reading database ... 60%
  ;;           | (Reading database ... 65%
  ;;           | (Reading database ... 70%
  ;;           | (Reading database ... 75%
  ;;           | (Reading database ... 80%
  ;;           | (Reading database ... 85%
  ;;           | (Reading database ... 90%
  ;;           | (Reading database ... 95%
  ;;           | (Reading database ... 100%
  ;;           | (Reading database ... 53234 files and directories currently installed.)
  ;;           | Preparing to replace sudo 1.8.3p1-1ubuntu3.2 (using .../sudo_1.8.3p1-1ubuntu3.4_amd64.deb) ...
  ;;           | Unpacking replacement sudo ...
  ;;           | Processing triggers for ureadahead ...
  ;;           | ureadahead will be reprofiled on next reboot
  ;;           | Processing triggers for man-db ...
  ;;           | Setting up sudo (1.8.3p1-1ubuntu3.4) ...
  ;;           | Installing new version of config file /etc/pam.d/sudo ...
  ;;           | accountsservice					install
  ;;           | acpid						install
  ;;           | adduser						install
  ;;           | apparmor					install
  ;;           | apport						install
  ;;           | apport-symptoms					install
  ;;           | apt						install
  ;;           | apt-transport-https				install
  ;;           | apt-utils					install
  ;;           | apt-xapian-index				install
  ;;           | aptitude					install
  ;;           | at						install
  ;;           | base-files					install
  ;;           | base-passwd					install
  ;;           | bash						install
  ;;           | bash-completion					install
  ;;           | bc						install
  ;;           | bind9-host					install
  ;;           | binutils					install
  ;;           | bsdmainutils					install
  ;;           | bsdutils					install
  ;;           | build-essential					install
  ;;           | busybox-initramfs				install
  ;;           | busybox-static					install
  ;;           | byobu						install
  ;;           | bzip2						install
  ;;           | ca-certificates					install
  ;;           | command-not-found				install
  ;;           | command-not-found-data				install
  ;;           | console-setup					install
  ;;           | coreutils					install
  ;;           | cpio						install
  ;;           | cpp						install
  ;;           | cpp-4.6						install
  ;;           | crda						install
  ;;           | cron						install
  ;;           | curl						install
  ;;           | dash						install
  ;;           | dbus						install
  ;;           | debconf						install
  ;;           | debconf-i18n					install
  ;;           | debianutils					install
  ;;           | diffutils					install
  ;;           | dmidecode					install
  ;;           | dmsetup						install
  ;;           | dnsutils					install
  ;;           | dosfstools					install
  ;;           | dpkg						install
  ;;           | dpkg-dev					install
  ;;           | e2fslibs					install
  ;;           | e2fsprogs					install
  ;;           | ed						install
  ;;           | eject						install
  ;;           | fakeroot					install
  ;;           | file						install
  ;;           | findutils					install
  ;;           | fonts-ubuntu-font-family-console		install
  ;;           | friendly-recovery				install
  ;;           | ftp						install
  ;;           | fuse						install
  ;;           | g++						install
  ;;           | g++-4.6						install
  ;;           | gcc						install
  ;;           | gcc-4.6						install
  ;;           | gcc-4.6-base					install
  ;;           | geoip-database					install
  ;;           | gettext-base					install
  ;;           | gir1.2-glib-2.0					install
  ;;           | gnupg						install
  ;;           | gpgv						install
  ;;           | grep						install
  ;;           | groff-base					install
  ;;           | grub-common					install
  ;;           | grub-gfxpayload-lists				install
  ;;           | grub-pc						install
  ;;           | grub-pc-bin					install
  ;;           | grub2-common					install
  ;;           | gzip						install
  ;;           | hdparm						install
  ;;           | hostname					install
  ;;           | ifupdown					install
  ;;           | info						install
  ;;           | initramfs-tools					install
  ;;           | initramfs-tools-bin				install
  ;;           | initscripts					install
  ;;           | insserv						install
  ;;           | install-info					install
  ;;           | installation-report				install
  ;;           | iproute						install
  ;;           | iptables					install
  ;;           | iputils-ping					install
  ;;           | iputils-tracepath				install
  ;;           | irqbalance					install
  ;;           | isc-dhcp-client					install
  ;;           | isc-dhcp-common					install
  ;;           | iso-codes					install
  ;;           | kbd						install
  ;;           | keyboard-configuration				install
  ;;           | klibc-utils					install
  ;;           | krb5-locales					install
  ;;           | landscape-common				install
  ;;           | language-pack-en				install
  ;;           | language-pack-en-base				install
  ;;           | language-selector-common			install
  ;;           | laptop-detect					install
  ;;           | less						install
  ;;           | libaccountsservice0				install
  ;;           | libacl1						install
  ;;           | libalgorithm-diff-perl				install
  ;;           | libalgorithm-diff-xs-perl			install
  ;;           | libalgorithm-merge-perl				install
  ;;           | libapt-inst1.4					install
  ;;           | libapt-pkg4.12					install
  ;;           | libasn1-8-heimdal				install
  ;;           | libattr1					install
  ;;           | libbind9-80					install
  ;;           | libblkid1					install
  ;;           | libboost-iostreams1.46.1			install
  ;;           | libbsd0						install
  ;;           | libbz2-1.0					install
  ;;           | libc-bin					install
  ;;           | libc-dev-bin					install
  ;;           | libc6						install
  ;;           | libc6-dev					install
  ;;           | libcap-ng0					install
  ;;           | libclass-accessor-perl				install
  ;;           | libclass-isa-perl				install
  ;;           | libcomerr2					install
  ;;           | libcurl3					install
  ;;           | libcurl3-gnutls					install
  ;;           | libcwidget3					install
  ;;           | libdb5.1					install
  ;;           | libdbus-1-3					install
  ;;           | libdbus-glib-1-2				install
  ;;           | libdevmapper1.02.1				install
  ;;           | libdns81					install
  ;;           | libdpkg-perl					install
  ;;           | libdrm-intel1					install
  ;;           | libdrm-nouveau1a				install
  ;;           | libdrm-radeon1					install
  ;;           | libdrm2						install
  ;;           | libedit2					install
  ;;           | libelf1						install
  ;;           | libept1.4.12					install
  ;;           | libevent-2.0-5					install
  ;;           | libexpat1					install
  ;;           | libffi6						install
  ;;           | libfreetype6					install
  ;;           | libfribidi0					install
  ;;           | libfuse2					install
  ;;           | libgc1c2					install
  ;;           | libgcc1						install
  ;;           | libgcrypt11					install
  ;;           | libgdbm3					install
  ;;           | libgeoip1					install
  ;;           | libgirepository-1.0-1				install
  ;;           | libglib2.0-0					install
  ;;           | libgmp10					install
  ;;           | libgnutls26					install
  ;;           | libgomp1					install
  ;;           | libgpg-error0					install
  ;;           | libgpm2						install
  ;;           | libgssapi-krb5-2				install
  ;;           | libgssapi3-heimdal				install
  ;;           | libhcrypto4-heimdal				install
  ;;           | libheimbase1-heimdal				install
  ;;           | libheimntlm0-heimdal				install
  ;;           | libhx509-5-heimdal				install
  ;;           | libidn11					install
  ;;           | libio-string-perl				install
  ;;           | libisc83					install
  ;;           | libisccc80					install
  ;;           | libisccfg82					install
  ;;           | libiw30						install
  ;;           | libjs-jquery					install
  ;;           | libk5crypto3					install
  ;;           | libkeyutils1					install
  ;;           | libklibc					install
  ;;           | libkrb5-26-heimdal				install
  ;;           | libkrb5-3					install
  ;;           | libkrb5support0					install
  ;;           | libldap-2.4-2					install
  ;;           | liblocale-gettext-perl				install
  ;;           | liblockfile-bin					install
  ;;           | liblockfile1					install
  ;;           | liblwres80					install
  ;;           | liblzma5					install
  ;;           | libmagic1					install
  ;;           | libmount1					install
  ;;           | libmpc2						install
  ;;           | libmpfr4					install
  ;;           | libncurses5					install
  ;;           | libncursesw5					install
  ;;           | libnewt0.52					install
  ;;           | libnfnetlink0					install
  ;;           | libnih-dbus1					install
  ;;           | libnih1						install
  ;;           | libnl-3-200					install
  ;;           | libnl-genl-3-200				install
  ;;           | libp11-kit0					install
  ;;           | libpam-modules					install
  ;;           | libpam-modules-bin				install
  ;;           | libpam-runtime					install
  ;;           | libpam0g					install
  ;;           | libparse-debianchangelog-perl			install
  ;;           | libparted0debian1				install
  ;;           | libpcap0.8					install
  ;;           | libpci3						install
  ;;           | libpciaccess0					install
  ;;           | libpcre3					install
  ;;           | libpcsclite1					install
  ;;           | libpipeline1					install
  ;;           | libplymouth2					install
  ;;           | libpng12-0					install
  ;;           | libpolkit-gobject-1-0				install
  ;;           | libpopt0					install
  ;;           | libpython2.7					install
  ;;           | libquadmath0					install
  ;;           | libreadline6					install
  ;;           | libroken18-heimdal				install
  ;;           | librtmp0					install
  ;;           | libsasl2-2					install
  ;;           | libsasl2-modules				install
  ;;           | libselinux1					install
  ;;           | libsigc++-2.0-0c2a				install
  ;;           | libslang2					install
  ;;           | libsqlite3-0					install
  ;;           | libss2						install
  ;;           | libssl1.0.0					install
  ;;           | libstdc++6					install
  ;;           | libstdc++6-4.6-dev				install
  ;;           | libsub-name-perl				install
  ;;           | libswitch-perl					install
  ;;           | libtasn1-3					install
  ;;           | libtext-charwidth-perl				install
  ;;           | libtext-iconv-perl				install
  ;;           | libtext-wrapi18n-perl				install
  ;;           | libtimedate-perl				install
  ;;           | libtinfo5					install
  ;;           | libudev0					install
  ;;           | libusb-0.1-4					install
  ;;           | libusb-1.0-0					install
  ;;           | libuuid1					install
  ;;           | libwind0-heimdal				install
  ;;           | libwrap0					install
  ;;           | libx11-6					install
  ;;           | libx11-data					install
  ;;           | libxapian22					install
  ;;           | libxau6						install
  ;;           | libxcb1						install
  ;;           | libxdmcp6					install
  ;;           | libxext6					install
  ;;           | libxml2						install
  ;;           | libxmuu1					install
  ;;           | linux-firmware					install
  ;;           | linux-headers-3.2.0-23				install
  ;;           | linux-headers-3.2.0-23-generic			install
  ;;           | linux-headers-server				install
  ;;           | linux-image-3.2.0-23-generic			install
  ;;           | linux-image-server				install
  ;;           | linux-libc-dev					install
  ;;           | linux-server					install
  ;;           | locales						install
  ;;           | lockfile-progs					install
  ;;           | login						install
  ;;           | logrotate					install
  ;;           | lsb-base					install
  ;;           | lsb-release					install
  ;;           | lshw						install
  ;;           | lsof						install
  ;;           | ltrace						install
  ;;           | make						install
  ;;           | makedev						install
  ;;           | man-db						install
  ;;           | manpages					install
  ;;           | manpages-dev					install
  ;;           | mawk						install
  ;;           | memtest86+					install
  ;;           | mime-support					install
  ;;           | mlocate						install
  ;;           | module-assistant				install
  ;;           | module-init-tools				install
  ;;           | mount						install
  ;;           | mountall					install
  ;;           | mtr-tiny					install
  ;;           | multiarch-support				install
  ;;           | nano						install
  ;;           | ncurses-base					install
  ;;           | ncurses-bin					install
  ;;           | net-tools					install
  ;;           | netbase						install
  ;;           | netcat-openbsd					install
  ;;           | ntfs-3g						install
  ;;           | ntpdate						install
  ;;           | openssh-client					install
  ;;           | openssh-server					install
  ;;           | openssl						install
  ;;           | os-prober					install
  ;;           | parted						install
  ;;           | passwd						install
  ;;           | patch						install
  ;;           | pciutils					install
  ;;           | perl						install
  ;;           | perl-base					install
  ;;           | perl-modules					install
  ;;           | plymouth					install
  ;;           | plymouth-theme-ubuntu-text			install
  ;;           | popularity-contest				install
  ;;           | powermgmt-base					install
  ;;           | ppp						install
  ;;           | pppconfig					install
  ;;           | pppoeconf					install
  ;;           | procps						install
  ;;           | psmisc						install
  ;;           | python						install
  ;;           | python-apport					install
  ;;           | python-apt					install
  ;;           | python-apt-common				install
  ;;           | python-chardet					install
  ;;           | python-crypto					install
  ;;           | python-dbus					install
  ;;           | python-dbus-dev					install
  ;;           | python-debian					install
  ;;           | python-gdbm					install
  ;;           | python-gi					install
  ;;           | python-gnupginterface				install
  ;;           | python-httplib2					install
  ;;           | python-keyring					install
  ;;           | python-launchpadlib				install
  ;;           | python-lazr.restfulclient			install
  ;;           | python-lazr.uri					install
  ;;           | python-minimal					install
  ;;           | python-newt					install
  ;;           | python-oauth					install
  ;;           | python-openssl					install
  ;;           | python-pam					install
  ;;           | python-pkg-resources				install
  ;;           | python-problem-report				install
  ;;           | python-serial					install
  ;;           | python-simplejson				install
  ;;           | python-twisted-bin				install
  ;;           | python-twisted-core				install
  ;;           | python-wadllib					install
  ;;           | python-xapian					install
  ;;           | python-zope.interface				install
  ;;           | python2.7					install
  ;;           | python2.7-minimal				install
  ;;           | readline-common					install
  ;;           | resolvconf					install
  ;;           | rsync						install
  ;;           | rsyslog						install
  ;;           | screen						install
  ;;           | sed						install
  ;;           | sensible-utils					install
  ;;           | sgml-base					install
  ;;           | ssh-import-id					install
  ;;           | strace						install
  ;;           | sudo						install
  ;;           | sysv-rc						install
  ;;           | sysvinit-utils					install
  ;;           | tar						install
  ;;           | tasksel						install
  ;;           | tasksel-data					install
  ;;           | tcpd						install
  ;;           | tcpdump						install
  ;;           | telnet						install
  ;;           | time						install
  ;;           | tmux						install
  ;;           | tzdata						install
  ;;           | ubuntu-keyring					install
  ;;           | ubuntu-minimal					install
  ;;           | ubuntu-standard					install
  ;;           | ucf						install
  ;;           | udev						install
  ;;           | ufw						install
  ;;           | update-manager-core				install
  ;;           | update-notifier-common				install
  ;;           | upstart						install
  ;;           | ureadahead					install
  ;;           | usbutils					install
  ;;           | util-linux					install
  ;;           | uuid-runtime					install
  ;;           | vim						install
  ;;           | vim-common					install
  ;;           | vim-runtime					install
  ;;           | vim-tiny					install
  ;;           | w3m						install
  ;;           | wget						install
  ;;           | whiptail					install
  ;;           | whoopsie					install
  ;;           | wireless-regdb					install
  ;;           | wireless-tools					install
  ;;           | wpasupplicant					install
  ;;           | xauth						install
  ;;           | xkb-data					install
  ;;           | xml-core					install
  ;;           | xz-lzma						install
  ;;           | xz-utils					install
  ;;           | zlib1g						install
  ;;           | #> [automated-admin-user: install]: Packages : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | if getent passwd tbatchelli; then /usr/sbin/usermod --shell "/bin/bash" tbatchelli;else /usr/sbin/useradd --shell "/bin/bash" --create-home tbatchelli;fi
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: 
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...';
  ;;           | {
  ;;           | mkdir -m "755" -p $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chown --recursive tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chmod 755 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...';
  ;;           | {
  ;;           | touch $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chown tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chmod 644 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...';
  ;;           | {
  ;;           | auth_file=$(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && if ! ( fgrep "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local" ${auth_file} ); then
  ;;           | echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local
  ;;           | " >> ${auth_file}
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...';
  ;;           | {
  ;;           | if hash chcon 2>&- && [ -d /etc/selinux ] && [ -e /selinux/enforce ] && stat --format %C $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ 2>&-; then chcon -Rv --type=user_home_t $(getent passwd tbatchelli | cut -d: -f6)/.ssh/;fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: sudoers: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: sudoers: remote-file /etc/sudoers...';
  ;;           | {
  ;;           | filediff= && if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | filediff=$?
  ;;           | fi && md5diff= && if [ -e /var/lib/pallet/etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers.md5 ]; then
  ;;           | ( cd $(dirname /var/lib/pallet/etc/sudoers.md5) && md5sum --quiet --check $(basename /var/lib/pallet/etc/sudoers.md5) )
  ;;           | md5diff=$?
  ;;           | fi && errexit=0 && if [ "${filediff}" == "1" ]; then
  ;;           | echo Existing file did not match the pallet master copy: FAIL
  ;;           | errexit=1
  ;;           | fi && if [ "${md5diff}" == "1" ]; then
  ;;           | echo Existing content did not match md5: FAIL
  ;;           | errexit=1
  ;;           | fi && [ "${errexit}" == "0" ] && mkdir -p $(dirname /tmp/root/bBCdbpTJf7UZAHwfBogFgg) && { cat > /tmp/root/bBCdbpTJf7UZAHwfBogFgg <<EOFpallet
  ;;           | Defaults env_keep=SSH_AUTH_SOCK
  ;;           | root ALL = (ALL) ALL
  ;;           | %adm ALL = (ALL) ALL
  ;;           | tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | EOFpallet
  ;;           |  } && contentdiff= && if [ -e /etc/sudoers ] && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | diff -u /etc/sudoers /tmp/root/bBCdbpTJf7UZAHwfBogFgg
  ;;           | contentdiff=$?
  ;;           | fi && if ! { [ "${contentdiff}" == "0" ]; } && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | chown root /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chgrp $(id -ng root) /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chmod 0440 /tmp/root/bBCdbpTJf7UZAHwfBogFgg && mv -f /tmp/root/bBCdbpTJf7UZAHwfBogFgg /etc/sudoers && dirpath=$(dirname /var/lib/pallet/etc/sudoers)
  ;;           | templatepath=$(dirname $(if [ -e /etc/sudoers ]; then readlink -f /etc/sudoers;else echo /etc/sudoers;fi))
  ;;           | if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi
  ;;           | templatepath=$(readlink -f ${templatepath})
  ;;           | mkdir -p ${dirpath} || exit 1
  ;;           | while [ "/" != "${templatepath}" ] ;do d=${dirpath} && t=${templatepath} && if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi && dirpath=$(dirname ${dirpath}) && templatepath=$(dirname ${templatepath}) && chgrp $(stat -c%G ${t}) ${d} || : && chmod $(stat -c%a ${t}) ${d} || : && chown $(stat -c%U ${t}) ${d} || : ; done && contentdiff=
  ;;           | if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | contentdiff=$?
  ;;           | fi
  ;;           | if ! { [ "${contentdiff}" == "0" ]; } && [ -e /etc/sudoers ]; then cp -f --backup="numbered" /etc/sudoers /var/lib/pallet/etc/sudoers;fi && ls -t /var/lib/pallet/etc/sudoers.~[0-9]*~ 2> /dev/null | tail -n "+6" | xargs \
  ;;           |  rm --force && (cp=$(readlink -f /etc/sudoers) && cd $(dirname ${cp}) && md5sum $(basename ${cp})
  ;;           | )>/var/lib/pallet/etc/sudoers.md5 && echo MD5 sum is $(cat /var/lib/pallet/etc/sudoers.md5)
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: sudoers: remote-file /etc/sudoers...
  ;;           | --- /etc/sudoers	2012-01-31 10:56:42.000000000 -0500
  ;;           | +++ /tmp/root/bBCdbpTJf7UZAHwfBogFgg	2014-01-16 14:28:56.060013930 -0500
  ;;           | @@ -1,29 +1,4 @@
  ;;           | -#
  ;;           | -# This file MUST be edited with the 'visudo' command as root.
  ;;           | -#
  ;;           | -# Please consider adding local content in /etc/sudoers.d/ instead of
  ;;           | -# directly modifying this file.
  ;;           | -#
  ;;           | -# See the man page for details on how to write a sudoers file.
  ;;           | -#
  ;;           | -Defaults	env_reset
  ;;           | -Defaults	secure_path="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
  ;;           | -
  ;;           | -# Host alias specification
  ;;           | -
  ;;           | -# User alias specification
  ;;           | -
  ;;           | -# Cmnd alias specification
  ;;           | -
  ;;           | -# User privilege specification
  ;;           | -root	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# Members of the admin group may gain root privileges
  ;;           | -%admin ALL=(ALL) ALL
  ;;           | -
  ;;           | -# Allow members of group sudo to execute any command
  ;;           | -%sudo	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# See sudoers(5) for more information on "#include" directives:
  ;;           | -
  ;;           | -#includedir /etc/sudoers.d
  ;;           | +Defaults env_keep=SSH_AUTH_SOCK
  ;;           | +root ALL = (ALL) ALL
  ;;           | +%adm ALL = (ALL) ALL
  ;;           | +tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | MD5 sum is 7c74ebd65015e958c87276681e97de4b sudoers
  ;;           | #> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS
  ;;     GROUP good:
  ;;       NODE 192.168.56.109:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'package-manager update ...';
  ;;           | {
  ;;           | apt-get -qq update
  ;;           |  } || { echo '#> package-manager update  : FAIL'; exit 1;} >&2 
  ;;           | echo '#> package-manager update  : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: package-manager update ...
  ;;           | #> package-manager update  : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user: install]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo '[automated-admin-user: install]: Packages...';
  ;;           | {
  ;;           | { debconf-set-selections <<EOF
  ;;           | debconf debconf/frontend select noninteractive
  ;;           | debconf debconf/frontend seen false
  ;;           | EOF
  ;;           | } && enableStart() {
  ;;           | rm /usr/sbin/policy-rc.d
  ;;           | } && apt-get -q -y install sudo+ && dpkg --get-selections
  ;;           |  } || { echo '#> [automated-admin-user: install]: Packages : FAIL'; exit 1;} >&2 
  ;;           | echo '#> [automated-admin-user: install]: Packages : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: [automated-admin-user: install]: Packages...
  ;;           | Reading package lists...
  ;;           | Building dependency tree...
  ;;           | Reading state information...
  ;;           | The following packages will be upgraded:
  ;;           |   sudo
  ;;           | 1 upgraded, 0 newly installed, 0 to remove and 163 not upgraded.
  ;;           | Need to get 288 kB of archives.
  ;;           | After this operation, 16.4 kB disk space will be freed.
  ;;           | Get:1 http://us.archive.ubuntu.com/ubuntu/ precise-updates/main sudo amd64 1.8.3p1-1ubuntu3.4 [288 kB]
  ;;           | Fetched 288 kB in 2s (127 kB/s)
  ;;           | (Reading database ... 
  ;;           | (Reading database ... 5%
  ;;           | (Reading database ... 10%
  ;;           | (Reading database ... 15%
  ;;           | (Reading database ... 20%
  ;;           | (Reading database ... 25%
  ;;           | (Reading database ... 30%
  ;;           | (Reading database ... 35%
  ;;           | (Reading database ... 40%
  ;;           | (Reading database ... 45%
  ;;           | (Reading database ... 50%
  ;;           | (Reading database ... 55%
  ;;           | (Reading database ... 60%
  ;;           | (Reading database ... 65%
  ;;           | (Reading database ... 70%
  ;;           | (Reading database ... 75%
  ;;           | (Reading database ... 80%
  ;;           | (Reading database ... 85%
  ;;           | (Reading database ... 90%
  ;;           | (Reading database ... 95%
  ;;           | (Reading database ... 100%
  ;;           | (Reading database ... 53234 files and directories currently installed.)
  ;;           | Preparing to replace sudo 1.8.3p1-1ubuntu3.2 (using .../sudo_1.8.3p1-1ubuntu3.4_amd64.deb) ...
  ;;           | Unpacking replacement sudo ...
  ;;           | Processing triggers for ureadahead ...
  ;;           | ureadahead will be reprofiled on next reboot
  ;;           | Processing triggers for man-db ...
  ;;           | Setting up sudo (1.8.3p1-1ubuntu3.4) ...
  ;;           | Installing new version of config file /etc/pam.d/sudo ...
  ;;           | accountsservice					install
  ;;           | acpid						install
  ;;           | adduser						install
  ;;           | apparmor					install
  ;;           | apport						install
  ;;           | apport-symptoms					install
  ;;           | apt						install
  ;;           | apt-transport-https				install
  ;;           | apt-utils					install
  ;;           | apt-xapian-index				install
  ;;           | aptitude					install
  ;;           | at						install
  ;;           | base-files					install
  ;;           | base-passwd					install
  ;;           | bash						install
  ;;           | bash-completion					install
  ;;           | bc						install
  ;;           | bind9-host					install
  ;;           | binutils					install
  ;;           | bsdmainutils					install
  ;;           | bsdutils					install
  ;;           | build-essential					install
  ;;           | busybox-initramfs				install
  ;;           | busybox-static					install
  ;;           | byobu						install
  ;;           | bzip2						install
  ;;           | ca-certificates					install
  ;;           | command-not-found				install
  ;;           | command-not-found-data				install
  ;;           | console-setup					install
  ;;           | coreutils					install
  ;;           | cpio						install
  ;;           | cpp						install
  ;;           | cpp-4.6						install
  ;;           | crda						install
  ;;           | cron						install
  ;;           | curl						install
  ;;           | dash						install
  ;;           | dbus						install
  ;;           | debconf						install
  ;;           | debconf-i18n					install
  ;;           | debianutils					install
  ;;           | diffutils					install
  ;;           | dmidecode					install
  ;;           | dmsetup						install
  ;;           | dnsutils					install
  ;;           | dosfstools					install
  ;;           | dpkg						install
  ;;           | dpkg-dev					install
  ;;           | e2fslibs					install
  ;;           | e2fsprogs					install
  ;;           | ed						install
  ;;           | eject						install
  ;;           | fakeroot					install
  ;;           | file						install
  ;;           | findutils					install
  ;;           | fonts-ubuntu-font-family-console		install
  ;;           | friendly-recovery				install
  ;;           | ftp						install
  ;;           | fuse						install
  ;;           | g++						install
  ;;           | g++-4.6						install
  ;;           | gcc						install
  ;;           | gcc-4.6						install
  ;;           | gcc-4.6-base					install
  ;;           | geoip-database					install
  ;;           | gettext-base					install
  ;;           | gir1.2-glib-2.0					install
  ;;           | gnupg						install
  ;;           | gpgv						install
  ;;           | grep						install
  ;;           | groff-base					install
  ;;           | grub-common					install
  ;;           | grub-gfxpayload-lists				install
  ;;           | grub-pc						install
  ;;           | grub-pc-bin					install
  ;;           | grub2-common					install
  ;;           | gzip						install
  ;;           | hdparm						install
  ;;           | hostname					install
  ;;           | ifupdown					install
  ;;           | info						install
  ;;           | initramfs-tools					install
  ;;           | initramfs-tools-bin				install
  ;;           | initscripts					install
  ;;           | insserv						install
  ;;           | install-info					install
  ;;           | installation-report				install
  ;;           | iproute						install
  ;;           | iptables					install
  ;;           | iputils-ping					install
  ;;           | iputils-tracepath				install
  ;;           | irqbalance					install
  ;;           | isc-dhcp-client					install
  ;;           | isc-dhcp-common					install
  ;;           | iso-codes					install
  ;;           | kbd						install
  ;;           | keyboard-configuration				install
  ;;           | klibc-utils					install
  ;;           | krb5-locales					install
  ;;           | landscape-common				install
  ;;           | language-pack-en				install
  ;;           | language-pack-en-base				install
  ;;           | language-selector-common			install
  ;;           | laptop-detect					install
  ;;           | less						install
  ;;           | libaccountsservice0				install
  ;;           | libacl1						install
  ;;           | libalgorithm-diff-perl				install
  ;;           | libalgorithm-diff-xs-perl			install
  ;;           | libalgorithm-merge-perl				install
  ;;           | libapt-inst1.4					install
  ;;           | libapt-pkg4.12					install
  ;;           | libasn1-8-heimdal				install
  ;;           | libattr1					install
  ;;           | libbind9-80					install
  ;;           | libblkid1					install
  ;;           | libboost-iostreams1.46.1			install
  ;;           | libbsd0						install
  ;;           | libbz2-1.0					install
  ;;           | libc-bin					install
  ;;           | libc-dev-bin					install
  ;;           | libc6						install
  ;;           | libc6-dev					install
  ;;           | libcap-ng0					install
  ;;           | libclass-accessor-perl				install
  ;;           | libclass-isa-perl				install
  ;;           | libcomerr2					install
  ;;           | libcurl3					install
  ;;           | libcurl3-gnutls					install
  ;;           | libcwidget3					install
  ;;           | libdb5.1					install
  ;;           | libdbus-1-3					install
  ;;           | libdbus-glib-1-2				install
  ;;           | libdevmapper1.02.1				install
  ;;           | libdns81					install
  ;;           | libdpkg-perl					install
  ;;           | libdrm-intel1					install
  ;;           | libdrm-nouveau1a				install
  ;;           | libdrm-radeon1					install
  ;;           | libdrm2						install
  ;;           | libedit2					install
  ;;           | libelf1						install
  ;;           | libept1.4.12					install
  ;;           | libevent-2.0-5					install
  ;;           | libexpat1					install
  ;;           | libffi6						install
  ;;           | libfreetype6					install
  ;;           | libfribidi0					install
  ;;           | libfuse2					install
  ;;           | libgc1c2					install
  ;;           | libgcc1						install
  ;;           | libgcrypt11					install
  ;;           | libgdbm3					install
  ;;           | libgeoip1					install
  ;;           | libgirepository-1.0-1				install
  ;;           | libglib2.0-0					install
  ;;           | libgmp10					install
  ;;           | libgnutls26					install
  ;;           | libgomp1					install
  ;;           | libgpg-error0					install
  ;;           | libgpm2						install
  ;;           | libgssapi-krb5-2				install
  ;;           | libgssapi3-heimdal				install
  ;;           | libhcrypto4-heimdal				install
  ;;           | libheimbase1-heimdal				install
  ;;           | libheimntlm0-heimdal				install
  ;;           | libhx509-5-heimdal				install
  ;;           | libidn11					install
  ;;           | libio-string-perl				install
  ;;           | libisc83					install
  ;;           | libisccc80					install
  ;;           | libisccfg82					install
  ;;           | libiw30						install
  ;;           | libjs-jquery					install
  ;;           | libk5crypto3					install
  ;;           | libkeyutils1					install
  ;;           | libklibc					install
  ;;           | libkrb5-26-heimdal				install
  ;;           | libkrb5-3					install
  ;;           | libkrb5support0					install
  ;;           | libldap-2.4-2					install
  ;;           | liblocale-gettext-perl				install
  ;;           | liblockfile-bin					install
  ;;           | liblockfile1					install
  ;;           | liblwres80					install
  ;;           | liblzma5					install
  ;;           | libmagic1					install
  ;;           | libmount1					install
  ;;           | libmpc2						install
  ;;           | libmpfr4					install
  ;;           | libncurses5					install
  ;;           | libncursesw5					install
  ;;           | libnewt0.52					install
  ;;           | libnfnetlink0					install
  ;;           | libnih-dbus1					install
  ;;           | libnih1						install
  ;;           | libnl-3-200					install
  ;;           | libnl-genl-3-200				install
  ;;           | libp11-kit0					install
  ;;           | libpam-modules					install
  ;;           | libpam-modules-bin				install
  ;;           | libpam-runtime					install
  ;;           | libpam0g					install
  ;;           | libparse-debianchangelog-perl			install
  ;;           | libparted0debian1				install
  ;;           | libpcap0.8					install
  ;;           | libpci3						install
  ;;           | libpciaccess0					install
  ;;           | libpcre3					install
  ;;           | libpcsclite1					install
  ;;           | libpipeline1					install
  ;;           | libplymouth2					install
  ;;           | libpng12-0					install
  ;;           | libpolkit-gobject-1-0				install
  ;;           | libpopt0					install
  ;;           | libpython2.7					install
  ;;           | libquadmath0					install
  ;;           | libreadline6					install
  ;;           | libroken18-heimdal				install
  ;;           | librtmp0					install
  ;;           | libsasl2-2					install
  ;;           | libsasl2-modules				install
  ;;           | libselinux1					install
  ;;           | libsigc++-2.0-0c2a				install
  ;;           | libslang2					install
  ;;           | libsqlite3-0					install
  ;;           | libss2						install
  ;;           | libssl1.0.0					install
  ;;           | libstdc++6					install
  ;;           | libstdc++6-4.6-dev				install
  ;;           | libsub-name-perl				install
  ;;           | libswitch-perl					install
  ;;           | libtasn1-3					install
  ;;           | libtext-charwidth-perl				install
  ;;           | libtext-iconv-perl				install
  ;;           | libtext-wrapi18n-perl				install
  ;;           | libtimedate-perl				install
  ;;           | libtinfo5					install
  ;;           | libudev0					install
  ;;           | libusb-0.1-4					install
  ;;           | libusb-1.0-0					install
  ;;           | libuuid1					install
  ;;           | libwind0-heimdal				install
  ;;           | libwrap0					install
  ;;           | libx11-6					install
  ;;           | libx11-data					install
  ;;           | libxapian22					install
  ;;           | libxau6						install
  ;;           | libxcb1						install
  ;;           | libxdmcp6					install
  ;;           | libxext6					install
  ;;           | libxml2						install
  ;;           | libxmuu1					install
  ;;           | linux-firmware					install
  ;;           | linux-headers-3.2.0-23				install
  ;;           | linux-headers-3.2.0-23-generic			install
  ;;           | linux-headers-server				install
  ;;           | linux-image-3.2.0-23-generic			install
  ;;           | linux-image-server				install
  ;;           | linux-libc-dev					install
  ;;           | linux-server					install
  ;;           | locales						install
  ;;           | lockfile-progs					install
  ;;           | login						install
  ;;           | logrotate					install
  ;;           | lsb-base					install
  ;;           | lsb-release					install
  ;;           | lshw						install
  ;;           | lsof						install
  ;;           | ltrace						install
  ;;           | make						install
  ;;           | makedev						install
  ;;           | man-db						install
  ;;           | manpages					install
  ;;           | manpages-dev					install
  ;;           | mawk						install
  ;;           | memtest86+					install
  ;;           | mime-support					install
  ;;           | mlocate						install
  ;;           | module-assistant				install
  ;;           | module-init-tools				install
  ;;           | mount						install
  ;;           | mountall					install
  ;;           | mtr-tiny					install
  ;;           | multiarch-support				install
  ;;           | nano						install
  ;;           | ncurses-base					install
  ;;           | ncurses-bin					install
  ;;           | net-tools					install
  ;;           | netbase						install
  ;;           | netcat-openbsd					install
  ;;           | ntfs-3g						install
  ;;           | ntpdate						install
  ;;           | openssh-client					install
  ;;           | openssh-server					install
  ;;           | openssl						install
  ;;           | os-prober					install
  ;;           | parted						install
  ;;           | passwd						install
  ;;           | patch						install
  ;;           | pciutils					install
  ;;           | perl						install
  ;;           | perl-base					install
  ;;           | perl-modules					install
  ;;           | plymouth					install
  ;;           | plymouth-theme-ubuntu-text			install
  ;;           | popularity-contest				install
  ;;           | powermgmt-base					install
  ;;           | ppp						install
  ;;           | pppconfig					install
  ;;           | pppoeconf					install
  ;;           | procps						install
  ;;           | psmisc						install
  ;;           | python						install
  ;;           | python-apport					install
  ;;           | python-apt					install
  ;;           | python-apt-common				install
  ;;           | python-chardet					install
  ;;           | python-crypto					install
  ;;           | python-dbus					install
  ;;           | python-dbus-dev					install
  ;;           | python-debian					install
  ;;           | python-gdbm					install
  ;;           | python-gi					install
  ;;           | python-gnupginterface				install
  ;;           | python-httplib2					install
  ;;           | python-keyring					install
  ;;           | python-launchpadlib				install
  ;;           | python-lazr.restfulclient			install
  ;;           | python-lazr.uri					install
  ;;           | python-minimal					install
  ;;           | python-newt					install
  ;;           | python-oauth					install
  ;;           | python-openssl					install
  ;;           | python-pam					install
  ;;           | python-pkg-resources				install
  ;;           | python-problem-report				install
  ;;           | python-serial					install
  ;;           | python-simplejson				install
  ;;           | python-twisted-bin				install
  ;;           | python-twisted-core				install
  ;;           | python-wadllib					install
  ;;           | python-xapian					install
  ;;           | python-zope.interface				install
  ;;           | python2.7					install
  ;;           | python2.7-minimal				install
  ;;           | readline-common					install
  ;;           | resolvconf					install
  ;;           | rsync						install
  ;;           | rsyslog						install
  ;;           | screen						install
  ;;           | sed						install
  ;;           | sensible-utils					install
  ;;           | sgml-base					install
  ;;           | ssh-import-id					install
  ;;           | strace						install
  ;;           | sudo						install
  ;;           | sysv-rc						install
  ;;           | sysvinit-utils					install
  ;;           | tar						install
  ;;           | tasksel						install
  ;;           | tasksel-data					install
  ;;           | tcpd						install
  ;;           | tcpdump						install
  ;;           | telnet						install
  ;;           | time						install
  ;;           | tmux						install
  ;;           | tzdata						install
  ;;           | ubuntu-keyring					install
  ;;           | ubuntu-minimal					install
  ;;           | ubuntu-standard					install
  ;;           | ucf						install
  ;;           | udev						install
  ;;           | ufw						install
  ;;           | update-manager-core				install
  ;;           | update-notifier-common				install
  ;;           | upstart						install
  ;;           | ureadahead					install
  ;;           | usbutils					install
  ;;           | util-linux					install
  ;;           | uuid-runtime					install
  ;;           | vim						install
  ;;           | vim-common					install
  ;;           | vim-runtime					install
  ;;           | vim-tiny					install
  ;;           | w3m						install
  ;;           | wget						install
  ;;           | whiptail					install
  ;;           | whoopsie					install
  ;;           | wireless-regdb					install
  ;;           | wireless-tools					install
  ;;           | wpasupplicant					install
  ;;           | xauth						install
  ;;           | xkb-data					install
  ;;           | xml-core					install
  ;;           | xz-lzma						install
  ;;           | xz-utils					install
  ;;           | zlib1g						install
  ;;           | #> [automated-admin-user: install]: Packages : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | if getent passwd tbatchelli; then /usr/sbin/usermod --shell "/bin/bash" tbatchelli;else /usr/sbin/useradd --shell "/bin/bash" --create-home tbatchelli;fi
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: 
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...';
  ;;           | {
  ;;           | mkdir -m "755" -p $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chown --recursive tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chmod 755 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...';
  ;;           | {
  ;;           | touch $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chown tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chmod 644 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...';
  ;;           | {
  ;;           | auth_file=$(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && if ! ( fgrep "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local" ${auth_file} ); then
  ;;           | echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local
  ;;           | " >> ${auth_file}
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...';
  ;;           | {
  ;;           | if hash chcon 2>&- && [ -d /etc/selinux ] && [ -e /selinux/enforce ] && stat --format %C $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ 2>&-; then chcon -Rv --type=user_home_t $(getent passwd tbatchelli | cut -d: -f6)/.ssh/;fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: sudoers: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: sudoers: remote-file /etc/sudoers...';
  ;;           | {
  ;;           | filediff= && if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | filediff=$?
  ;;           | fi && md5diff= && if [ -e /var/lib/pallet/etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers.md5 ]; then
  ;;           | ( cd $(dirname /var/lib/pallet/etc/sudoers.md5) && md5sum --quiet --check $(basename /var/lib/pallet/etc/sudoers.md5) )
  ;;           | md5diff=$?
  ;;           | fi && errexit=0 && if [ "${filediff}" == "1" ]; then
  ;;           | echo Existing file did not match the pallet master copy: FAIL
  ;;           | errexit=1
  ;;           | fi && if [ "${md5diff}" == "1" ]; then
  ;;           | echo Existing content did not match md5: FAIL
  ;;           | errexit=1
  ;;           | fi && [ "${errexit}" == "0" ] && mkdir -p $(dirname /tmp/root/bBCdbpTJf7UZAHwfBogFgg) && { cat > /tmp/root/bBCdbpTJf7UZAHwfBogFgg <<EOFpallet
  ;;           | Defaults env_keep=SSH_AUTH_SOCK
  ;;           | root ALL = (ALL) ALL
  ;;           | %adm ALL = (ALL) ALL
  ;;           | tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | EOFpallet
  ;;           |  } && contentdiff= && if [ -e /etc/sudoers ] && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | diff -u /etc/sudoers /tmp/root/bBCdbpTJf7UZAHwfBogFgg
  ;;           | contentdiff=$?
  ;;           | fi && if ! { [ "${contentdiff}" == "0" ]; } && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | chown root /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chgrp $(id -ng root) /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chmod 0440 /tmp/root/bBCdbpTJf7UZAHwfBogFgg && mv -f /tmp/root/bBCdbpTJf7UZAHwfBogFgg /etc/sudoers && dirpath=$(dirname /var/lib/pallet/etc/sudoers)
  ;;           | templatepath=$(dirname $(if [ -e /etc/sudoers ]; then readlink -f /etc/sudoers;else echo /etc/sudoers;fi))
  ;;           | if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi
  ;;           | templatepath=$(readlink -f ${templatepath})
  ;;           | mkdir -p ${dirpath} || exit 1
  ;;           | while [ "/" != "${templatepath}" ] ;do d=${dirpath} && t=${templatepath} && if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi && dirpath=$(dirname ${dirpath}) && templatepath=$(dirname ${templatepath}) && chgrp $(stat -c%G ${t}) ${d} || : && chmod $(stat -c%a ${t}) ${d} || : && chown $(stat -c%U ${t}) ${d} || : ; done && contentdiff=
  ;;           | if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | contentdiff=$?
  ;;           | fi
  ;;           | if ! { [ "${contentdiff}" == "0" ]; } && [ -e /etc/sudoers ]; then cp -f --backup="numbered" /etc/sudoers /var/lib/pallet/etc/sudoers;fi && ls -t /var/lib/pallet/etc/sudoers.~[0-9]*~ 2> /dev/null | tail -n "+6" | xargs \
  ;;           |  rm --force && (cp=$(readlink -f /etc/sudoers) && cd $(dirname ${cp}) && md5sum $(basename ${cp})
  ;;           | )>/var/lib/pallet/etc/sudoers.md5 && echo MD5 sum is $(cat /var/lib/pallet/etc/sudoers.md5)
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: sudoers: remote-file /etc/sudoers...
  ;;           | --- /etc/sudoers	2012-01-31 10:56:42.000000000 -0500
  ;;           | +++ /tmp/root/bBCdbpTJf7UZAHwfBogFgg	2014-01-16 14:31:05.629305149 -0500
  ;;           | @@ -1,29 +1,4 @@
  ;;           | -#
  ;;           | -# This file MUST be edited with the 'visudo' command as root.
  ;;           | -#
  ;;           | -# Please consider adding local content in /etc/sudoers.d/ instead of
  ;;           | -# directly modifying this file.
  ;;           | -#
  ;;           | -# See the man page for details on how to write a sudoers file.
  ;;           | -#
  ;;           | -Defaults	env_reset
  ;;           | -Defaults	secure_path="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
  ;;           | -
  ;;           | -# Host alias specification
  ;;           | -
  ;;           | -# User alias specification
  ;;           | -
  ;;           | -# Cmnd alias specification
  ;;           | -
  ;;           | -# User privilege specification
  ;;           | -root	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# Members of the admin group may gain root privileges
  ;;           | -%admin ALL=(ALL) ALL
  ;;           | -
  ;;           | -# Allow members of group sudo to execute any command
  ;;           | -%sudo	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# See sudoers(5) for more information on "#include" directives:
  ;;           | -
  ;;           | -#includedir /etc/sudoers.d
  ;;           | +Defaults env_keep=SSH_AUTH_SOCK
  ;;           | +root ALL = (ALL) ALL
  ;;           | +%adm ALL = (ALL) ALL
  ;;           | +tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | MD5 sum is 7c74ebd65015e958c87276681e97de4b sudoers
  ;;           | #> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS
  ;;       NODE 192.168.56.111:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'package-manager update ...';
  ;;           | {
  ;;           | apt-get -qq update
  ;;           |  } || { echo '#> package-manager update  : FAIL'; exit 1;} >&2 
  ;;           | echo '#> package-manager update  : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: package-manager update ...
  ;;           | #> package-manager update  : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user: install]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo '[automated-admin-user: install]: Packages...';
  ;;           | {
  ;;           | { debconf-set-selections <<EOF
  ;;           | debconf debconf/frontend select noninteractive
  ;;           | debconf debconf/frontend seen false
  ;;           | EOF
  ;;           | } && enableStart() {
  ;;           | rm /usr/sbin/policy-rc.d
  ;;           | } && apt-get -q -y install sudo+ && dpkg --get-selections
  ;;           |  } || { echo '#> [automated-admin-user: install]: Packages : FAIL'; exit 1;} >&2 
  ;;           | echo '#> [automated-admin-user: install]: Packages : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: [automated-admin-user: install]: Packages...
  ;;           | Reading package lists...
  ;;           | Building dependency tree...
  ;;           | Reading state information...
  ;;           | The following packages will be upgraded:
  ;;           |   sudo
  ;;           | 1 upgraded, 0 newly installed, 0 to remove and 163 not upgraded.
  ;;           | Need to get 288 kB of archives.
  ;;           | After this operation, 16.4 kB disk space will be freed.
  ;;           | Get:1 http://us.archive.ubuntu.com/ubuntu/ precise-updates/main sudo amd64 1.8.3p1-1ubuntu3.4 [288 kB]
  ;;           | Fetched 288 kB in 5s (52.9 kB/s)
  ;;           | (Reading database ... 
  ;;           | (Reading database ... 5%
  ;;           | (Reading database ... 10%
  ;;           | (Reading database ... 15%
  ;;           | (Reading database ... 20%
  ;;           | (Reading database ... 25%
  ;;           | (Reading database ... 30%
  ;;           | (Reading database ... 35%
  ;;           | (Reading database ... 40%
  ;;           | (Reading database ... 45%
  ;;           | (Reading database ... 50%
  ;;           | (Reading database ... 55%
  ;;           | (Reading database ... 60%
  ;;           | (Reading database ... 65%
  ;;           | (Reading database ... 70%
  ;;           | (Reading database ... 75%
  ;;           | (Reading database ... 80%
  ;;           | (Reading database ... 85%
  ;;           | (Reading database ... 90%
  ;;           | (Reading database ... 95%
  ;;           | (Reading database ... 100%
  ;;           | (Reading database ... 53234 files and directories currently installed.)
  ;;           | Preparing to replace sudo 1.8.3p1-1ubuntu3.2 (using .../sudo_1.8.3p1-1ubuntu3.4_amd64.deb) ...
  ;;           | Unpacking replacement sudo ...
  ;;           | Processing triggers for ureadahead ...
  ;;           | ureadahead will be reprofiled on next reboot
  ;;           | Processing triggers for man-db ...
  ;;           | Setting up sudo (1.8.3p1-1ubuntu3.4) ...
  ;;           | Installing new version of config file /etc/pam.d/sudo ...
  ;;           | accountsservice					install
  ;;           | acpid						install
  ;;           | adduser						install
  ;;           | apparmor					install
  ;;           | apport						install
  ;;           | apport-symptoms					install
  ;;           | apt						install
  ;;           | apt-transport-https				install
  ;;           | apt-utils					install
  ;;           | apt-xapian-index				install
  ;;           | aptitude					install
  ;;           | at						install
  ;;           | base-files					install
  ;;           | base-passwd					install
  ;;           | bash						install
  ;;           | bash-completion					install
  ;;           | bc						install
  ;;           | bind9-host					install
  ;;           | binutils					install
  ;;           | bsdmainutils					install
  ;;           | bsdutils					install
  ;;           | build-essential					install
  ;;           | busybox-initramfs				install
  ;;           | busybox-static					install
  ;;           | byobu						install
  ;;           | bzip2						install
  ;;           | ca-certificates					install
  ;;           | command-not-found				install
  ;;           | command-not-found-data				install
  ;;           | console-setup					install
  ;;           | coreutils					install
  ;;           | cpio						install
  ;;           | cpp						install
  ;;           | cpp-4.6						install
  ;;           | crda						install
  ;;           | cron						install
  ;;           | curl						install
  ;;           | dash						install
  ;;           | dbus						install
  ;;           | debconf						install
  ;;           | debconf-i18n					install
  ;;           | debianutils					install
  ;;           | diffutils					install
  ;;           | dmidecode					install
  ;;           | dmsetup						install
  ;;           | dnsutils					install
  ;;           | dosfstools					install
  ;;           | dpkg						install
  ;;           | dpkg-dev					install
  ;;           | e2fslibs					install
  ;;           | e2fsprogs					install
  ;;           | ed						install
  ;;           | eject						install
  ;;           | fakeroot					install
  ;;           | file						install
  ;;           | findutils					install
  ;;           | fonts-ubuntu-font-family-console		install
  ;;           | friendly-recovery				install
  ;;           | ftp						install
  ;;           | fuse						install
  ;;           | g++						install
  ;;           | g++-4.6						install
  ;;           | gcc						install
  ;;           | gcc-4.6						install
  ;;           | gcc-4.6-base					install
  ;;           | geoip-database					install
  ;;           | gettext-base					install
  ;;           | gir1.2-glib-2.0					install
  ;;           | gnupg						install
  ;;           | gpgv						install
  ;;           | grep						install
  ;;           | groff-base					install
  ;;           | grub-common					install
  ;;           | grub-gfxpayload-lists				install
  ;;           | grub-pc						install
  ;;           | grub-pc-bin					install
  ;;           | grub2-common					install
  ;;           | gzip						install
  ;;           | hdparm						install
  ;;           | hostname					install
  ;;           | ifupdown					install
  ;;           | info						install
  ;;           | initramfs-tools					install
  ;;           | initramfs-tools-bin				install
  ;;           | initscripts					install
  ;;           | insserv						install
  ;;           | install-info					install
  ;;           | installation-report				install
  ;;           | iproute						install
  ;;           | iptables					install
  ;;           | iputils-ping					install
  ;;           | iputils-tracepath				install
  ;;           | irqbalance					install
  ;;           | isc-dhcp-client					install
  ;;           | isc-dhcp-common					install
  ;;           | iso-codes					install
  ;;           | kbd						install
  ;;           | keyboard-configuration				install
  ;;           | klibc-utils					install
  ;;           | krb5-locales					install
  ;;           | landscape-common				install
  ;;           | language-pack-en				install
  ;;           | language-pack-en-base				install
  ;;           | language-selector-common			install
  ;;           | laptop-detect					install
  ;;           | less						install
  ;;           | libaccountsservice0				install
  ;;           | libacl1						install
  ;;           | libalgorithm-diff-perl				install
  ;;           | libalgorithm-diff-xs-perl			install
  ;;           | libalgorithm-merge-perl				install
  ;;           | libapt-inst1.4					install
  ;;           | libapt-pkg4.12					install
  ;;           | libasn1-8-heimdal				install
  ;;           | libattr1					install
  ;;           | libbind9-80					install
  ;;           | libblkid1					install
  ;;           | libboost-iostreams1.46.1			install
  ;;           | libbsd0						install
  ;;           | libbz2-1.0					install
  ;;           | libc-bin					install
  ;;           | libc-dev-bin					install
  ;;           | libc6						install
  ;;           | libc6-dev					install
  ;;           | libcap-ng0					install
  ;;           | libclass-accessor-perl				install
  ;;           | libclass-isa-perl				install
  ;;           | libcomerr2					install
  ;;           | libcurl3					install
  ;;           | libcurl3-gnutls					install
  ;;           | libcwidget3					install
  ;;           | libdb5.1					install
  ;;           | libdbus-1-3					install
  ;;           | libdbus-glib-1-2				install
  ;;           | libdevmapper1.02.1				install
  ;;           | libdns81					install
  ;;           | libdpkg-perl					install
  ;;           | libdrm-intel1					install
  ;;           | libdrm-nouveau1a				install
  ;;           | libdrm-radeon1					install
  ;;           | libdrm2						install
  ;;           | libedit2					install
  ;;           | libelf1						install
  ;;           | libept1.4.12					install
  ;;           | libevent-2.0-5					install
  ;;           | libexpat1					install
  ;;           | libffi6						install
  ;;           | libfreetype6					install
  ;;           | libfribidi0					install
  ;;           | libfuse2					install
  ;;           | libgc1c2					install
  ;;           | libgcc1						install
  ;;           | libgcrypt11					install
  ;;           | libgdbm3					install
  ;;           | libgeoip1					install
  ;;           | libgirepository-1.0-1				install
  ;;           | libglib2.0-0					install
  ;;           | libgmp10					install
  ;;           | libgnutls26					install
  ;;           | libgomp1					install
  ;;           | libgpg-error0					install
  ;;           | libgpm2						install
  ;;           | libgssapi-krb5-2				install
  ;;           | libgssapi3-heimdal				install
  ;;           | libhcrypto4-heimdal				install
  ;;           | libheimbase1-heimdal				install
  ;;           | libheimntlm0-heimdal				install
  ;;           | libhx509-5-heimdal				install
  ;;           | libidn11					install
  ;;           | libio-string-perl				install
  ;;           | libisc83					install
  ;;           | libisccc80					install
  ;;           | libisccfg82					install
  ;;           | libiw30						install
  ;;           | libjs-jquery					install
  ;;           | libk5crypto3					install
  ;;           | libkeyutils1					install
  ;;           | libklibc					install
  ;;           | libkrb5-26-heimdal				install
  ;;           | libkrb5-3					install
  ;;           | libkrb5support0					install
  ;;           | libldap-2.4-2					install
  ;;           | liblocale-gettext-perl				install
  ;;           | liblockfile-bin					install
  ;;           | liblockfile1					install
  ;;           | liblwres80					install
  ;;           | liblzma5					install
  ;;           | libmagic1					install
  ;;           | libmount1					install
  ;;           | libmpc2						install
  ;;           | libmpfr4					install
  ;;           | libncurses5					install
  ;;           | libncursesw5					install
  ;;           | libnewt0.52					install
  ;;           | libnfnetlink0					install
  ;;           | libnih-dbus1					install
  ;;           | libnih1						install
  ;;           | libnl-3-200					install
  ;;           | libnl-genl-3-200				install
  ;;           | libp11-kit0					install
  ;;           | libpam-modules					install
  ;;           | libpam-modules-bin				install
  ;;           | libpam-runtime					install
  ;;           | libpam0g					install
  ;;           | libparse-debianchangelog-perl			install
  ;;           | libparted0debian1				install
  ;;           | libpcap0.8					install
  ;;           | libpci3						install
  ;;           | libpciaccess0					install
  ;;           | libpcre3					install
  ;;           | libpcsclite1					install
  ;;           | libpipeline1					install
  ;;           | libplymouth2					install
  ;;           | libpng12-0					install
  ;;           | libpolkit-gobject-1-0				install
  ;;           | libpopt0					install
  ;;           | libpython2.7					install
  ;;           | libquadmath0					install
  ;;           | libreadline6					install
  ;;           | libroken18-heimdal				install
  ;;           | librtmp0					install
  ;;           | libsasl2-2					install
  ;;           | libsasl2-modules				install
  ;;           | libselinux1					install
  ;;           | libsigc++-2.0-0c2a				install
  ;;           | libslang2					install
  ;;           | libsqlite3-0					install
  ;;           | libss2						install
  ;;           | libssl1.0.0					install
  ;;           | libstdc++6					install
  ;;           | libstdc++6-4.6-dev				install
  ;;           | libsub-name-perl				install
  ;;           | libswitch-perl					install
  ;;           | libtasn1-3					install
  ;;           | libtext-charwidth-perl				install
  ;;           | libtext-iconv-perl				install
  ;;           | libtext-wrapi18n-perl				install
  ;;           | libtimedate-perl				install
  ;;           | libtinfo5					install
  ;;           | libudev0					install
  ;;           | libusb-0.1-4					install
  ;;           | libusb-1.0-0					install
  ;;           | libuuid1					install
  ;;           | libwind0-heimdal				install
  ;;           | libwrap0					install
  ;;           | libx11-6					install
  ;;           | libx11-data					install
  ;;           | libxapian22					install
  ;;           | libxau6						install
  ;;           | libxcb1						install
  ;;           | libxdmcp6					install
  ;;           | libxext6					install
  ;;           | libxml2						install
  ;;           | libxmuu1					install
  ;;           | linux-firmware					install
  ;;           | linux-headers-3.2.0-23				install
  ;;           | linux-headers-3.2.0-23-generic			install
  ;;           | linux-headers-server				install
  ;;           | linux-image-3.2.0-23-generic			install
  ;;           | linux-image-server				install
  ;;           | linux-libc-dev					install
  ;;           | linux-server					install
  ;;           | locales						install
  ;;           | lockfile-progs					install
  ;;           | login						install
  ;;           | logrotate					install
  ;;           | lsb-base					install
  ;;           | lsb-release					install
  ;;           | lshw						install
  ;;           | lsof						install
  ;;           | ltrace						install
  ;;           | make						install
  ;;           | makedev						install
  ;;           | man-db						install
  ;;           | manpages					install
  ;;           | manpages-dev					install
  ;;           | mawk						install
  ;;           | memtest86+					install
  ;;           | mime-support					install
  ;;           | mlocate						install
  ;;           | module-assistant				install
  ;;           | module-init-tools				install
  ;;           | mount						install
  ;;           | mountall					install
  ;;           | mtr-tiny					install
  ;;           | multiarch-support				install
  ;;           | nano						install
  ;;           | ncurses-base					install
  ;;           | ncurses-bin					install
  ;;           | net-tools					install
  ;;           | netbase						install
  ;;           | netcat-openbsd					install
  ;;           | ntfs-3g						install
  ;;           | ntpdate						install
  ;;           | openssh-client					install
  ;;           | openssh-server					install
  ;;           | openssl						install
  ;;           | os-prober					install
  ;;           | parted						install
  ;;           | passwd						install
  ;;           | patch						install
  ;;           | pciutils					install
  ;;           | perl						install
  ;;           | perl-base					install
  ;;           | perl-modules					install
  ;;           | plymouth					install
  ;;           | plymouth-theme-ubuntu-text			install
  ;;           | popularity-contest				install
  ;;           | powermgmt-base					install
  ;;           | ppp						install
  ;;           | pppconfig					install
  ;;           | pppoeconf					install
  ;;           | procps						install
  ;;           | psmisc						install
  ;;           | python						install
  ;;           | python-apport					install
  ;;           | python-apt					install
  ;;           | python-apt-common				install
  ;;           | python-chardet					install
  ;;           | python-crypto					install
  ;;           | python-dbus					install
  ;;           | python-dbus-dev					install
  ;;           | python-debian					install
  ;;           | python-gdbm					install
  ;;           | python-gi					install
  ;;           | python-gnupginterface				install
  ;;           | python-httplib2					install
  ;;           | python-keyring					install
  ;;           | python-launchpadlib				install
  ;;           | python-lazr.restfulclient			install
  ;;           | python-lazr.uri					install
  ;;           | python-minimal					install
  ;;           | python-newt					install
  ;;           | python-oauth					install
  ;;           | python-openssl					install
  ;;           | python-pam					install
  ;;           | python-pkg-resources				install
  ;;           | python-problem-report				install
  ;;           | python-serial					install
  ;;           | python-simplejson				install
  ;;           | python-twisted-bin				install
  ;;           | python-twisted-core				install
  ;;           | python-wadllib					install
  ;;           | python-xapian					install
  ;;           | python-zope.interface				install
  ;;           | python2.7					install
  ;;           | python2.7-minimal				install
  ;;           | readline-common					install
  ;;           | resolvconf					install
  ;;           | rsync						install
  ;;           | rsyslog						install
  ;;           | screen						install
  ;;           | sed						install
  ;;           | sensible-utils					install
  ;;           | sgml-base					install
  ;;           | ssh-import-id					install
  ;;           | strace						install
  ;;           | sudo						install
  ;;           | sysv-rc						install
  ;;           | sysvinit-utils					install
  ;;           | tar						install
  ;;           | tasksel						install
  ;;           | tasksel-data					install
  ;;           | tcpd						install
  ;;           | tcpdump						install
  ;;           | telnet						install
  ;;           | time						install
  ;;           | tmux						install
  ;;           | tzdata						install
  ;;           | ubuntu-keyring					install
  ;;           | ubuntu-minimal					install
  ;;           | ubuntu-standard					install
  ;;           | ucf						install
  ;;           | udev						install
  ;;           | ufw						install
  ;;           | update-manager-core				install
  ;;           | update-notifier-common				install
  ;;           | upstart						install
  ;;           | ureadahead					install
  ;;           | usbutils					install
  ;;           | util-linux					install
  ;;           | uuid-runtime					install
  ;;           | vim						install
  ;;           | vim-common					install
  ;;           | vim-runtime					install
  ;;           | vim-tiny					install
  ;;           | w3m						install
  ;;           | wget						install
  ;;           | whiptail					install
  ;;           | whoopsie					install
  ;;           | wireless-regdb					install
  ;;           | wireless-tools					install
  ;;           | wpasupplicant					install
  ;;           | xauth						install
  ;;           | xkb-data					install
  ;;           | xml-core					install
  ;;           | xz-lzma						install
  ;;           | xz-utils					install
  ;;           | zlib1g						install
  ;;           | #> [automated-admin-user: install]: Packages : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: [automated-admin-user]: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | if getent passwd tbatchelli; then /usr/sbin/usermod --shell "/bin/bash" tbatchelli;else /usr/sbin/useradd --shell "/bin/bash" --create-home tbatchelli;fi
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: 
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...';
  ;;           | {
  ;;           | mkdir -m "755" -p $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chown --recursive tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ && chmod 755 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Directory $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...';
  ;;           | {
  ;;           | touch $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chown tbatchelli $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && chmod 644 $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: file $(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...';
  ;;           | {
  ;;           | auth_file=$(getent passwd tbatchelli | cut -d: -f6)/.ssh/authorized_keys && if ! ( fgrep "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local" ${auth_file} ); then
  ;;           | echo "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6n/Xv0SAbH8feh7EN7jNPuDBbdGfY8QIoQT+iite8s/rz+lP9gnmjanT40B/sW+TCp/IvOrreBJRAM7Gkx7khN40PXT18fOTpEf5EfCyKmRqD8r9fvCDZ3YV3lQCwaZ3ebEJyBp7ULCso8QbEvcokL1F63rDLcUWiYFGZ5MWk2J0/Y/1es7BJfFzFgaqKtp9NABQvsAJdWnEYCtNZtTG+AzolIn1ru55gEOkZDpPLtqF/59YzCJx5YPx5w/MLrhgVOeggJbpvuTZWdpEK8srItXKJ2IIBK2kBLLWMMZ4iqHuQysbcyWp5PGI8F0R2s1DWQ7pHZtvFSQ5bWl71HDOZQ== tbatchelli@tbatchellis-laptop-2.local
  ;;           | " >> ${auth_file}
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: authorize-key on user tbatchelli (ssh_key.clj:32) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: authorize-user-key: authorize-key: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...';
  ;;           | {
  ;;           | if hash chcon 2>&- && [ -d /etc/selinux ] && [ -e /selinux/enforce ] && stat --format %C $(getent passwd tbatchelli | cut -d: -f6)/.ssh/ 2>&-; then chcon -Rv --type=user_home_t $(getent passwd tbatchelli | cut -d: -f6)/.ssh/;fi
  ;;           |  } || { echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37)...
  ;;           | #> automated-admin-user: authorize-user-key: authorize-key: Set selinux permissions (ssh_key.clj:37) : SUCCESS
  ;;         ACTION ON NODE:
  ;;           CONTEXT: automated-admin-user: sudoers: 
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'automated-admin-user: sudoers: remote-file /etc/sudoers...';
  ;;           | {
  ;;           | filediff= && if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | filediff=$?
  ;;           | fi && md5diff= && if [ -e /var/lib/pallet/etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers.md5 ]; then
  ;;           | ( cd $(dirname /var/lib/pallet/etc/sudoers.md5) && md5sum --quiet --check $(basename /var/lib/pallet/etc/sudoers.md5) )
  ;;           | md5diff=$?
  ;;           | fi && errexit=0 && if [ "${filediff}" == "1" ]; then
  ;;           | echo Existing file did not match the pallet master copy: FAIL
  ;;           | errexit=1
  ;;           | fi && if [ "${md5diff}" == "1" ]; then
  ;;           | echo Existing content did not match md5: FAIL
  ;;           | errexit=1
  ;;           | fi && [ "${errexit}" == "0" ] && mkdir -p $(dirname /tmp/root/bBCdbpTJf7UZAHwfBogFgg) && { cat > /tmp/root/bBCdbpTJf7UZAHwfBogFgg <<EOFpallet
  ;;           | Defaults env_keep=SSH_AUTH_SOCK
  ;;           | root ALL = (ALL) ALL
  ;;           | %adm ALL = (ALL) ALL
  ;;           | tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | EOFpallet
  ;;           |  } && contentdiff= && if [ -e /etc/sudoers ] && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | diff -u /etc/sudoers /tmp/root/bBCdbpTJf7UZAHwfBogFgg
  ;;           | contentdiff=$?
  ;;           | fi && if ! { [ "${contentdiff}" == "0" ]; } && [ -e /tmp/root/bBCdbpTJf7UZAHwfBogFgg ]; then
  ;;           | chown root /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chgrp $(id -ng root) /tmp/root/bBCdbpTJf7UZAHwfBogFgg && chmod 0440 /tmp/root/bBCdbpTJf7UZAHwfBogFgg && mv -f /tmp/root/bBCdbpTJf7UZAHwfBogFgg /etc/sudoers && dirpath=$(dirname /var/lib/pallet/etc/sudoers)
  ;;           | templatepath=$(dirname $(if [ -e /etc/sudoers ]; then readlink -f /etc/sudoers;else echo /etc/sudoers;fi))
  ;;           | if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi
  ;;           | templatepath=$(readlink -f ${templatepath})
  ;;           | mkdir -p ${dirpath} || exit 1
  ;;           | while [ "/" != "${templatepath}" ] ;do d=${dirpath} && t=${templatepath} && if ! { [ -d ${templatepath} ]; }; then
  ;;           | echo ${templatepath} : Directory does not exist.
  ;;           | exit 1
  ;;           | fi && dirpath=$(dirname ${dirpath}) && templatepath=$(dirname ${templatepath}) && chgrp $(stat -c%G ${t}) ${d} || : && chmod $(stat -c%a ${t}) ${d} || : && chown $(stat -c%U ${t}) ${d} || : ; done && contentdiff=
  ;;           | if [ -e /etc/sudoers ] && [ -e /var/lib/pallet/etc/sudoers ]; then
  ;;           | diff -u /etc/sudoers /var/lib/pallet/etc/sudoers
  ;;           | contentdiff=$?
  ;;           | fi
  ;;           | if ! { [ "${contentdiff}" == "0" ]; } && [ -e /etc/sudoers ]; then cp -f --backup="numbered" /etc/sudoers /var/lib/pallet/etc/sudoers;fi && ls -t /var/lib/pallet/etc/sudoers.~[0-9]*~ 2> /dev/null | tail -n "+6" | xargs \
  ;;           |  rm --force && (cp=$(readlink -f /etc/sudoers) && cd $(dirname ${cp}) && md5sum $(basename ${cp})
  ;;           | )>/var/lib/pallet/etc/sudoers.md5 && echo MD5 sum is $(cat /var/lib/pallet/etc/sudoers.md5)
  ;;           | fi
  ;;           |  } || { echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : FAIL'; exit 1;} >&2 
  ;;           | echo '#> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | [sudo] password for vmfest: automated-admin-user: sudoers: remote-file /etc/sudoers...
  ;;           | --- /etc/sudoers	2012-01-31 10:56:42.000000000 -0500
  ;;           | +++ /tmp/root/bBCdbpTJf7UZAHwfBogFgg	2014-01-16 14:30:10.025982137 -0500
  ;;           | @@ -1,29 +1,4 @@
  ;;           | -#
  ;;           | -# This file MUST be edited with the 'visudo' command as root.
  ;;           | -#
  ;;           | -# Please consider adding local content in /etc/sudoers.d/ instead of
  ;;           | -# directly modifying this file.
  ;;           | -#
  ;;           | -# See the man page for details on how to write a sudoers file.
  ;;           | -#
  ;;           | -Defaults	env_reset
  ;;           | -Defaults	secure_path="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
  ;;           | -
  ;;           | -# Host alias specification
  ;;           | -
  ;;           | -# User alias specification
  ;;           | -
  ;;           | -# Cmnd alias specification
  ;;           | -
  ;;           | -# User privilege specification
  ;;           | -root	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# Members of the admin group may gain root privileges
  ;;           | -%admin ALL=(ALL) ALL
  ;;           | -
  ;;           | -# Allow members of group sudo to execute any command
  ;;           | -%sudo	ALL=(ALL:ALL) ALL
  ;;           | -
  ;;           | -# See sudoers(5) for more information on "#include" directives:
  ;;           | -
  ;;           | -#includedir /etc/sudoers.d
  ;;           | +Defaults env_keep=SSH_AUTH_SOCK
  ;;           | +root ALL = (ALL) ALL
  ;;           | +%adm ALL = (ALL) ALL
  ;;           | +tbatchelli ALL = (ALL) NOPASSWD: ALL
  ;;           | MD5 sum is 7c74ebd65015e958c87276681e97de4b sudoers
  ;;           | #> automated-admin-user: sudoers: remote-file /etc/sudoers : SUCCESS
  ;;   PHASE first:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.110:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo first!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | first!
  ;;       NODE 192.168.56.108:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo first!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | first!
  ;;     GROUP good:
  ;;       NODE 192.168.56.111:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo first!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | first!
  ;;       NODE 192.168.56.109:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo first!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | first!
  ;;   PHASE second:
  ;;     GROUP bad:
  ;;       NODE 192.168.56.108:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo hello world!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | hello world!
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'fail! (session_results.clj:38)...';
  ;;           | {
  ;;           | exit -1
  ;;           |  } || { echo '#> fail! (session_results.clj:38) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> fail! (session_results.clj:38) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 255
  ;;           OUTPUT:
  ;;           | fail! (session_results.clj:38)...
  ;;           ERROR:
  ;;             TYPE: :pallet-script-excution-error
  ;;             MESSAGE: 192.168.56.108 Error executing script
  ;;             OUTPUT: fail! (session_results.clj:38)...
  ;;       NODE 192.168.56.110:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo hello world!
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | hello world!
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'fail! (session_results.clj:38)...';
  ;;           | {
  ;;           | exit -1
  ;;           |  } || { echo '#> fail! (session_results.clj:38) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> fail! (session_results.clj:38) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 255
  ;;           OUTPUT:
  ;;           | fail! (session_results.clj:38)...
  ;;           ERROR:
  ;;             TYPE: :pallet-script-excution-error
  ;;             MESSAGE: 192.168.56.110 Error executing script
  ;;             OUTPUT: fail! (session_results.clj:38)...
  ;;     GROUP good:
  ;;       NODE 192.168.56.109:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'say hello! (session_results.clj:24)...';
  ;;           | {
  ;;           | echo hello world!
  ;;           |  } || { echo '#> say hello! (session_results.clj:24) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> say hello! (session_results.clj:24) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | say hello! (session_results.clj:24)...
  ;;           | hello world!
  ;;           | #> say hello! (session_results.clj:24) : SUCCESS
  ;;       NODE 192.168.56.111:
  ;;         ACTION ON NODE:
  ;;           SCRIPT:
  ;;           | #!/usr/bin/env bash
  ;;           | set -h
  ;;           | echo 'say hello! (session_results.clj:24)...';
  ;;           | {
  ;;           | echo hello world!
  ;;           |  } || { echo '#> say hello! (session_results.clj:24) : FAIL'; exit 1;} >&2 
  ;;           | echo '#> say hello! (session_results.clj:24) : SUCCESS'
  ;;           | 
  ;;           | exit $?
  ;;           EXIT CODE: 0
  ;;           OUTPUT:
  ;;           | say hello! (session_results.clj:24)...
  ;;           | hello world!
  ;;           | #> say hello! (session_results.clj:24) : SUCCESS
  ;; nil
  ;; session-results> 
  ;;
  )
