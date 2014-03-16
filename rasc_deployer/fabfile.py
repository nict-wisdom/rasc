# -*- coding: utf-8 -*- 
# encoding: utf-8
from __future__ import with_statement
from fabric.api import *
from fabric.contrib.console import confirm
from fabric.contrib.files import upload_template,sed,exists
from fabric.decorators import roles
import xml.etree.ElementTree as ElementTree
import datetime
import sys
import ConfigParser
import os
import commands
import re
import glob
from xml.dom import minidom
import json

#sys.setdefaultencoding('UTF-8')
env.forward_agent = True
env.use_ssh_config = True

#########################################################
# General
#########################################################
repos_work='repos_work'
env.repos_update=True

def _clean():
	cmd = 'cd {0} ; '.format(repos_work)
	if env.repos_update==True:
		cmd = cmd + ' git reset --hard HEAD ; git checkout -f {0} '.format(env['repository']['repos.branch'])
	local(cmd)
	

def _make_host_list(hosts):
	host_list = []
	for (prefix, start, end) in hosts:
		host_list.extend([prefix + str(i) for i in range(start, end + 1)])
	return host_list

@runs_once
def _ant_clean():
	local('cd {0}/rasc_build ;ant clean'.format(repos_work))


def _make_services_list(sv):
	slist = []
	services = eval(env['services']['servicesdef'])
	if isinstance(sv,str):
		if sv == 'all':
			for k,v in services.items():
				slist.append(v)
	elif isinstance(sv,list):
		for s in sv:
			if services.has_key(s):
				slist.append(services[s])
	return slist

def _make_services_key_list(sv):
	slist = []
	services = eval(env['services']['servicesdef'])
	if isinstance(sv,str):
		if sv == 'all':
			for k,v in services.items():
				slist.append(k)
	elif isinstance(sv,list):
		for s in sv:
			if services.has_key(s):
				slist.append(s)
	return slist

def _patch_src(patchfile):
	print '## patchを当てます'
	local('cd {0} ; patch -p1 < ../{1}'.format(repos_work,patchfile))
	


#####################################
# config 設定ファイル読み込み
#####################################
def config(conf="wisdom2013.properties"):
	_read_config(conf)
	_check_config()

def _read_config(conf_file):
	#プロパティファイル読み込み
	print "## 設定ファイルを読み込みます ==>" + conf_file
	conf = ConfigParser.SafeConfigParser()
	conf.read(conf_file)
	for section in conf.sections():
		sect_conf = {}
		for attr, value in conf.items(section):
			sect_conf[attr] = value
		setattr(env, section, sect_conf)
	env.read_config=True

def _check_config():
	#設定ファイルのservices整合性をチェック
	workers =  eval(env['workers']['workers'])
	services = eval(env['services']['servicesdef'])
	servers =  eval(env['servers']['servers'])
	
	#worker
	for w in workers:
		worker = workers[w]
		slist = worker['services']
		if isinstance(slist,list):
			for sv in slist:
				if services.has_key(sv) == False:
					print '##WARN## {0} の {1} はservicesに存在しません'.format(w,sv)
	for s in servers:
		server = servers[s]
		slist = server['services']
		if isinstance(slist,list):
			for sv in slist:
				if services.has_key(sv) == False:
					print '##WARN## {0} の {1} はservicesに存在しません'.format(s,sv)
					return

	#server workerの整合性チェック	
	for s in servers:
		server = servers[s]
		for wl in server['workers']:
			if workers.has_key(wl)==False:
				print '##WARN## {0} の workers <{1}> はworkerリストに存在しません'.format(s,wl)
				return
	for s in servers:
		server = servers[s]
		for wl in server['workers']:
			worker = workers[wl]
			slist = _make_services_list(server['services'])
			wlist = _make_services_list(worker['services'])
			for sv in slist:
				if (sv in wlist) == False:
					if sv['worker']['path'] != 'None':
						print '##WARN## server {0} の services <{1}> はworkerのservicesリストに存在しません'.format(s,sv)
	

#####################################
# checkout リポジトリ取得
#####################################
def checkout(Update=True):
	env.repos_update = Update
	_checkout()

@runs_once
def _checkout():
	# リポジトリからチェックアウト、既に存在している場合には、updateする
	print "## リポジトリからチェックアウトします。"
	if os.path.exists(repos_work)==False:
		os.mkdir(repos_work)
#		local('hg clone {0} {1} -u {2} -b {3} '.format(env['repository']['repos.url'],repos_work,env['repository']['repos.branch'],env['repository']['repos.branch']))
		local('git clone {0} {1}'.format(env['repository']['repos.url'],repos_work))
	else:
		cmd = 'cd {0} ; '.format(repos_work)
		if env.repos_update==True:
#			cmd = cmd + 'hg pull ; hg update -C {0} '.format(env['repository']['repos.branch'])
                        cmd = cmd + 'git reset --hard HEAD ; git pull ../{0} ; git checkout -f {1}'.format(env['repository']['repos.url'], env['repository']['repos.branch'])
                local(cmd)


#####################################
# deployAllworkers  Workerの全系統を配備する
#####################################
def deployAllworkers(Update=True):
	print "## Workerの配備を開始します"
	env.repos_update = Update
	execute(checkout,Update)
	workers =  eval(env['workers']['workers'])
	for w in workers:
		execute(deployworkers,w,Update)

def deployworkers(w,Update=True):
	execute(checkout,Update)
	workers =  eval(env['workers']['workers'])
	print '## {0} の配備を開始します'.format(w)
	_worker_build(w)
	_update_worker_sh(w)
	worker = workers[w]
	env.roledefs.update({'workers' : _make_host_list(worker['host'])})
	execute(_deployworkers,w)
	

@runs_once
def _worker_build(w):
	# ant してビルドを行う
	_clean()
	if(env['workers'].has_key('patch.src')==True):
		_patch_src(env['workers']['patch.src'])
		
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	print '## {0} をビルドします'.format(w)
	local('cd {0}/rasc_build ; ant clean ; ant '.format(repos_work))

def _update_worker_sh(w):
	# 起動停止スクリプトを更新する
	print '## {0} の起動、停止スクリプトを更新します。'.format(w)
	sf = '{0}/{1}'.format(repos_work,env['workers']['scripts.start'])
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	services = _make_services_list(worker['services'])
	jsonfile = '{0}/{1}/worker.json'.format(repos_work,env['jetty']['jetty.script'])
	deploypath = env['deploy']['deploy.path'] + w + "/"

	cd = ' -json {0}{1}/worker.json'.format(deploypath,env['jetty']['jetty.script'])
	
	#起動サービス(JSON)書き出し
	jsonData = {}
	jsonData['serverName'] = w
	jsonData['jettyPort'] = worker['port']
	jsonData['controlPort'] = 0
	
	wars = []
	
	for sv in services:
		if sv['worker']['path'] != 'None':
			winfo = {}
			winfo['contextPath'] = '/{0}'.format(sv['worker']['path'])
			winfo['serviceName'] = sv['worker']['name']
			winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,sv['worker']['path'],sv['worker']['path'])
			winfo['msgpackPort'] = sv['msgpackPort']
			wars.append(winfo)
	jsonData['msgpackServices']=wars

	wars = []
	for sv in services:
		if sv['worker']['path'] != 'None':
			winfo = {}
			winfo['contextPath'] = '/{0}'.format(sv['worker']['path'])
			winfo['serviceName'] = sv['worker']['name']
			winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,sv['worker']['path'],sv['worker']['path'])
			#winfo['msgpackPort'] = sv['msgpackPort']
			wars.append(winfo)
	jsonData['httpServices']=wars

	jf = open(jsonfile,"w")
	jf.write(json.dumps(jsonData, sort_keys=True, indent=4))
	jf.close()

	cd = cd + ' -t $JettyTempPath'
	local('cat {0}  | sed -e \'s#^\\(java.* $EmbeddedJettyPath\\)\\([^>]*\\)#\\1 {1} #g\' | sed -e \'s#^\\(JettyTempPath=\\)\\(.*\\)#\\1{2}{3}/tmp #g\' > {4}'
	.format(sf,cd, env['deploy']['deploy.path'],w,sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))
	
	sf = '{0}/{1}'.format(repos_work,env['workers']['scripts.stop'])
	local('cat {0} | sed -e \'s#^\\([^/]*\\)\\(.*pid\\)#\\1 {1}{2}/tmp/runallworker.pid #g\' > {3}'.format(sf, env['deploy']['deploy.path'],w,sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))


#####################################
# deployAllservers  Serverの全系統を配備する
#####################################
def deployAllservers(Update=True):
	print "## Serverの配備を開始します"
	env.repos_update = Update
	execute(checkout,Update)
	servers =  eval(env['servers']['servers'])
	for s in servers:
		execute(deployservers,s,Update)

def deployservers(s,Update=True):
	execute(checkout,Update)
	servers =  eval(env['servers']['servers'])
	_clean()
	if(env['servers'].has_key('patch.src')==True):
		_patch_src(env['servers']['patch.src'])

	print '## {0} の配備を開始します'.format(s)
	server = servers[s]
	_server_build(s)
	_update_server_sh(s)
	env.roledefs.update({'servers' : _make_host_list(server['host'])})
	execute(_deployservers,s)


def _update_service_xml(s):
	#対象のWorkerに合わせて、Service.xmlを書き換える
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	w = server['workers']
	services = _make_services_list(server['services'])
	for sv in services:
		if sv['rewriteEndpoint'] == True:
			print sv['server']['path']
			_modifyEndponts(sv,w)

def _modifyEndponts(s,w):
	#サービスXML書き換え処理
	workers =  eval(env['workers']['workers'])
	worker = workers[w[0]]
	servicetypes = eval(env['servicetypes']['servicetypes'])
	files = glob.glob('{0}/{1}/WebContent/WEB-INF/services/{2}.xml'.format(repos_work,s['server']['path'],s['server']['name']))
        value = ""
	#サービスXMLの個数実施（通常は1)
	for xml in files:
		print xml
		f = open(xml, "r")
		try: 
			tree = ElementTree.parse(f)
			root = tree.getroot()
			list = root.find("./bean/property/bean/property/list")
			#serviceTypeで生成する
			if env['workers']['worker.servicetype'] == 'msgpackRPC':
				value = "http://111.111.111.111:8080/"
			else :
				value = 'http://111.111.111.111:8080/{0}/{1}/{2}'.format(s['worker']['path'],servicetypes[env['workers']['worker.servicetype']]['servletname'],s['worker']['name'])
			#既存のEndPointを除去
			for item in list.findall("./value"):
				list.remove(item)
			#新しいEndPointを追加
			for endpoint in _make_host_list(worker['host']):
				v = ElementTree.Element('value')
				if env['workers']['worker.servicetype'] == 'msgpackRPC':
					v.text = re.sub(r'http://[0-9.:]+/','http://{0}:{1}/'.format(endpoint,s['msgpackPort']),value)
				else:
					v.text = re.sub(r'http://[0-9.:]+/','http://{0}:{1}/'.format(endpoint,worker['port']),value)
				list.append(v)
				
			#TimeOut設定
			tm_match = False
			for tm in root.findall("./bean/property/bean/property"):
				if tm.get('name') == 'waitTimeOut':
					#上書き
					tm_match = True
					tm.set('value',str(s['timeout']))
			if tm_match == False:
				bean = root.find('./bean/property/bean')
				p = ElementTree.Element('property')
				p.set('name','waitTimeOut')
				p.set('value',str(s['timeout']))
				bean.append(p)

			#ClientFactory
			for cf in root.findall("./bean/property/bean/property"):
				if cf.get('name') == 'clientFactory':
					bean = cf.find('./bean')
					bean.set('class',servicetypes[env['workers']['worker.servicetype']]['factory'])
		finally:
			f.close()
			#サービスXML書き出し
			#beansのDOCTYPEが除去されるので補完する
			d = minidom.parseString(ElementTree.tostring(root)).toprettyxml(encoding="UTF-8")
			d = d.replace('<?xml version="1.0" encoding="UTF-8"?>',
			'<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">')
			f = open(xml,"w")
			f.write(d)
			f.close()
	#JSONのEndpointを書き出す
	jsonData = {}
	jsonData['base'] = value
	endpoints = []
	for idx in w:
		worker = workers[idx]
		ep = []
		for url in _make_host_list(worker['host']):
			if env['workers']['worker.servicetype'] == 'msgpackRPC':
				ep.append( re.sub(r'http://[0-9.:]+/','http://{0}:{1}/'.format(url,s['msgpackPort']),value))
			else:
				ep.append( re.sub(r'http://[0-9.:]+/','http://{0}:{1}/'.format(url,worker['port']),value))
		endpoints.append(ep)
	jsonData['endpoints'] = endpoints
	jf = open('{0}/{1}/WebContent/WEB-INF/endpoints.json'.format(repos_work,s['server']['path']),"w")
	jf.write(json.dumps(jsonData, sort_keys=True, indent=4))
	jf.close()
	
def _server_build(s):
	#serverのビルド
	_ant_clean()
	_update_service_xml(s)
	print '## {0} をビルドします'.format(s)
	local('cd {0}/rasc_build ;ant '.format(repos_work))

def _update_server_sh(s):
	# 起動停止スクリプトを更新する
	print '## {0} の起動、停止スクリプトを更新します。'.format(s)
	sf = '{0}/{1}'.format(repos_work,env['servers']['scripts.start'])
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	services = _make_services_list(server['services'])
	jsonfile = '{0}/{1}/server.json'.format(repos_work,env['jetty']['jetty.script'])
	deploypath = env['deploy']['deploy.path'] + s + "/"
	cd = ' -json {0}{1}/server.json'.format(deploypath,env['jetty']['jetty.script'])

	#起動サービス(JSON)書き出し
	jsonData = {}
	jsonData['serverName'] = s
	jsonData['jettyPort'] = server['port']
	jsonData['controlPort'] = 0
	
	wars = []

	for sv in services:
		winfo = {}
		winfo['contextPath'] = '/{0}'.format(sv['server']['path'])
		winfo['serviceName'] = sv['server']['name']
		winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,sv['server']['path'],sv['server']['path'])
		winfo['msgpackPort'] = sv['msgpackPort']
		wars.append(winfo)
		
	jsonData['msgpackServices']=wars

	wars = []
	for sv in services:
		winfo = {}
		winfo['contextPath'] = '/{0}'.format(sv['server']['path'])
		winfo['serviceName'] = sv['server']['name']
		winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,sv['server']['path'],sv['server']['path'])
		#winfo['msgpackPort'] = sv['msgpackPort']
		wars.append(winfo)

	jsonData['httpServices']=wars

	jf = open(jsonfile,"w")
	jf.write(json.dumps(jsonData, sort_keys=True, indent=4))
	jf.close()

	cd = cd + ' -t $JettyTempPath'
	local('cat {0}  | sed -e \'s#^\\(java.* $EmbeddedJettyPath\\)\\([^>]*\\)#\\1 {1} #g\' | sed -e \'s#^\\(JettyTempPath=\\)\\(.*\\)#\\1{2}{3}/tmp #g\' | sed -e \'s#^\\(RunAllServerPid=\\)\\(.*\\)#\\1$JettyTempPath/runallserver.pid;#g\' > {4}'
	.format(sf,cd, env['deploy']['deploy.path'],s,sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))
	
	sf = '{0}/{1}'.format(repos_work,env['servers']['scripts.stop'])
	local('cat {0} | sed -e \'s#^\\([^/]*\\)\\(.*pid\\)#\\1 {1}{2}/tmp/runallserver.pid #g\' > {3}'.format(sf, env['deploy']['deploy.path'],s,sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))


#####################################
# deployworkers  Workerを配備する
#####################################
@roles('workers')
@parallel
def _deployworkers(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	deploypath = env['deploy']['deploy.path'] + w 
	run('mkdir -p {0}/tmp'.format(deploypath))
	wars = _make_services_list(worker['services'])
	for sv in wars:
		if sv['worker']['path'] != 'None':
			run('mkdir -p {0}/{1}/war'.format(deploypath,sv['worker']['path']))
			put('{0}/{1}/war/*'.format(repos_work,sv['worker']['path']),'{0}/{1}/war'.format(deploypath,sv['worker']['path']))
	#jetty
	run('mkdir -p {0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
	put('{0}/{1}/*'.format(repos_work,env['jetty']['jetty.project']),'{0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
	run('chmod 777 {0}/{1}'.format(deploypath,env['workers']['scripts.start']))
	run('chmod 777 {0}/{1}'.format(deploypath,env['workers']['scripts.stop']))
	
#####################################
# deployservers  Serverを配備する
#####################################
@roles('servers')
@parallel
def _deployservers(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	services = _make_services_list(server['services'])
	deploypath = env['deploy']['deploy.path'] + s 
	run('mkdir -p {0}/tmp'.format(deploypath))
	for sv in services:
		run('mkdir -p {0}/{1}/war'.format(deploypath,sv['server']['path']))
		put('{0}/{1}/war/*'.format(repos_work,sv['server']['path']),'{0}/{1}/war'.format(deploypath,sv['server']['path']))
	#jetty
	run('mkdir -p {0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
	put('{0}/{1}/*'.format(repos_work,env['jetty']['jetty.project']),'{0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
	run('chmod 777 {0}/{1}'.format(deploypath,env['servers']['scripts.start']))
	run('chmod 777 {0}/{1}'.format(deploypath,env['servers']['scripts.stop']))

#####################################
# startworkers  Workerを起動
#####################################
def startworkers(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	env.roledefs.update({'workers' : _make_host_list(worker['host'])})
	execute(_startWorker,w)

#####################################
# stopworkers  Workerを停止
#####################################
def stopworkers(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	env.roledefs.update({'workers' : _make_host_list(worker['host'])})
	execute(_stopWorker,w)

#####################################
# startAllworkers  全系統のWokerを起動
#####################################
def startAllworkers():
	workers =  eval(env['workers']['workers'])
	for w in workers:
		execute(startworkers,w)

#####################################
# stopAllworkers  全系統のWokerを停止
#####################################
def stopAllworkers():
	workers =  eval(env['workers']['workers'])
	for w in workers:
		execute(stopworkers,w)

@roles('workers')
@parallel
def _startWorker(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	deploypath = env['deploy']['deploy.path'] + w + "/"
	run('{0}{1}'.format(deploypath,env['workers']['scripts.start']),shell=True,pty=False)

@roles('workers')
@parallel
def _stopWorker(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	deploypath = env['deploy']['deploy.path'] + w + "/"
	run('{0}{1}'.format(deploypath,env['workers']['scripts.stop']),shell=True,pty=False)



#####################################
# startservers  Serverを起動
#####################################
def startservers(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	env.roledefs.update({'servers' : _make_host_list(server['host'])})
	execute(_startserver,s)

#####################################
# stopservers  Serverを停止
#####################################
def stopservers(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	env.roledefs.update({'servers' : _make_host_list(server['host'])})
	execute(_stopserver,s)

#####################################
# startAllservers  全系統のServerを起動
#####################################
def startAllservers():
	servers =  eval(env['servers']['servers'])
	for s in servers:
		execute(startservers,s)

#####################################
# stopAllservers  全系統のServerを停止
#####################################
def stopAllservers():
	servers =  eval(env['servers']['servers'])
	for s in servers:
		execute(stopservers,s)


@roles('servers')
@parallel
def _startserver(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	deploypath = env['deploy']['deploy.path'] + s + "/"
	run('{0}{1}'.format(deploypath,env['servers']['scripts.start']),shell=True,pty=False)

@roles('servers')
@parallel
def _stopserver(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	deploypath = env['deploy']['deploy.path'] + s + "/"
	run('{0}{1}'.format(deploypath,env['servers']['scripts.stop']),shell=True,pty=False)



#####################################
# startservers  Serverを起動
#####################################
def cleanservers(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	execute(stopservers,s)
	env.roledefs.update({'servers' : _make_host_list(server['host'])})
	execute(_cleanserver,s)

#####################################
# startworkers  Workerを起動
#####################################
def cleanworkers(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	execute(stopworkers,w)
	env.roledefs.update({'workers' : _make_host_list(worker['host'])})
	execute(_cleanworker,w)

#####################################
# startAllservers  全系統のServerを起動
#####################################
def cleanAllservers():
	servers =  eval(env['servers']['servers'])
	for s in servers:
		execute(cleanservers,s)

#####################################
# startAllworkers  全系統のWokerを起動
#####################################
def cleanAllworkers():
	workers =  eval(env['workers']['workers'])
	for w in workers:
		execute(cleanworkers,w)


@roles('servers')
@parallel
def _cleanserver(s):
	servers =  eval(env['servers']['servers'])
	server = servers[s]
	deploypath = env['deploy']['deploy.path'] + s + "/"
	run('rm -rf {0}'.format(deploypath))

@roles('workers')
@parallel
def _cleanworker(w):
	workers =  eval(env['workers']['workers'])
	worker = workers[w]
	deploypath = env['deploy']['deploy.path'] + w + "/"
	run('rm -rf {0}'.format(deploypath))

#####################################
# deployproxy サービスProxyを配備
#####################################
def deployproxy(Update=True):
	execute(checkout,Update)
	proxy =  eval(env['proxyserver']['proxy'])
	servers =  eval(env['servers']['servers'])
	proxy_servers = []
	for s in servers:
		srv = {}
		srv['host']=_make_host_list(servers[s]['host'])
		srv['port']=servers[s]['port']
		srv['services']= _make_services_list(servers[s]['services'])
		srv['skey'] = _make_services_key_list(servers[s]['services'])
		proxy_servers.append(srv)
	if len(proxy_servers) >= 2:
		for i in range(0,len(proxy_servers) - 1):
			if len(proxy_servers[i]['services']) != len(proxy_servers[i+1]['services']):
				print '## Proxyを配備する場合には、serverのserviceを同じ物にしてください'
				return
	print '## Proxyの配備を開始します'
	_clean()
	_proxy_build(proxy_servers)
	_update_proxy_sh(proxy_servers)
	env.roledefs.update({'proxys' : _make_host_list(proxy['host'])})
	execute(_deployproxy)

def _proxy_build(slist):
	_ant_clean()
	_update_proxy_xml(slist)
	print '## Proxy をビルドします'
	local('cd {0}/rasc_build ;ant '.format(repos_work))


def _update_proxy_xml(slist):
	#Proxy用のサービスXMLを作成する。
	for s in slist[0]['skey']:
		_write_proxy_xml(s)
	
	#endpoint(JSON)書き出し
	jsonData = {}
	jsonData['base'] = ''
	endpoints = []
	for idx in slist:
		ep = []
		for h in idx['host']:
			url = 'http://{0}:{1}/'.format(h,idx['port'])
			ep.append(url)
		endpoints.append(ep)
	jsonData['endpoints'] = endpoints
	jf = open('{0}/{1}/WebContent/WEB-INF/endpoints.json'.format(repos_work,env['proxyserver']['target.project']),"w")
	jf.write(json.dumps(jsonData, sort_keys=True, indent=4))
	jf.close()
		
def _write_proxy_xml(s):
	#xml書き出し
#	interfaces = eval(env['interfaces']['interfaces'])
	services = eval(env['services']['servicesdef'])
	servers =  eval(env['servers']['servers'])
	serviceType = eval(env['servicetypes']['servicetypes'])
	st = serviceType[env['servers']['server.servicetype']]

	basexml = '{0}/{1}/WebContent/WEB-INF/services/ProxyBase.xml'.format(repos_work,env['proxyserver']['target.project'])
	f = open(basexml, "r")
	
	try: 
		tree = ElementTree.parse(f)
		root = tree.getroot()
		list = root.find("./bean/property/list")
		for item in list.findall("./value"):
			list.remove(item)
		v =  ElementTree.Element('value')
		v.text = services[s]['interface']
		list.append(v)
		
		tm_match = False
		for tm in root.findall("./bean/property/bean/property"):
			if tm.get('name') == 'serviceMapping':
				#上書き
				tm_match = True
				tm.set('value','{0}/{1}/{2}'.format(services[s]['server']['path'],st['servletname'],services[s]['server']['name']))
			elif tm.get('name') == 'clientFactory':
				#clientfactory設定
				bean = tm.find('./bean')
				bean.set('class',st['factory'])
			elif tm.get('name') == 'msgpackPort':
				tm.set('value','{0}'.format(services[s]['msgpackPort']))
				
		if tm_match == False:
			bean = root.find('./bean/property/bean')
			p = ElementTree.Element('property')
			p.set('name','serviceMapping')
			p.set('value','{0}/{1}/{2}'.format(services[s]['server']['path'],st['servletname'],services[s]['server']['name']))
			bean.append(p)
	finally:
		f.close()
		#サービスXML書き出し
		#beansのDOCTYPEが除去されるので補完する
		d = minidom.parseString(ElementTree.tostring(root)).toprettyxml(encoding="UTF-8")
		d = d.replace('<?xml version="1.0" encoding="UTF-8"?>',
		'<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">')
		xml = '{0}/{1}/WebContent/WEB-INF/services/{2}.xml'.format(repos_work,env['proxyserver']['target.project'],services[s]['server']['name'])
		f = open(xml,"w")
		f.write(d)
		f.close()
	
def _update_proxy_sh(slist):
	# 起動停止スクリプトを更新する
	print '## Proxyの起動、停止スクリプトを更新します。'
	services = eval(env['services']['servicesdef'])
	proxy =  eval(env['proxyserver']['proxy'])
	sf = '{0}/{1}'.format(repos_work,env['proxyserver']['scripts.start'])
	cd = ''
	deploypath = env['deploy']['deploy.path'] + "proxy" + "/"
	target = env['proxyserver']['target.project']
	jsonfile = '{0}/{1}/proxy.json'.format(repos_work,env['jetty']['jetty.script'])
	cd = ' -json {0}{1}/proxy.json'.format(deploypath,env['jetty']['jetty.script'])

	#起動サービス(JSON)書き出し
	jsonData = {}
	jsonData['serverName'] = 'ProxyServer'
	jsonData['jettyPort'] = proxy['port']
	jsonData['controlPort'] = 0
	
	wars = []

	for s in slist[0]['skey']:
		winfo = {}
		winfo['contextPath'] = '/{0}'.format(services[s]['server']['name'])
		winfo['serviceName'] = services[s]['server']['name']
		winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,target,target)
		winfo['msgpackPort'] = services[s]['msgpackPort']
		wars.append(winfo)
	jsonData['msgpackServices']=wars

	wars=[]

	winfo = {}
	winfo['contextPath'] = '/{0}'.format(target)
	winfo['serviceName'] = 'ProxyService'
	winfo['warPath'] = '{0}{1}/war/{2}.war'.format(deploypath,target,target)
	#winfo['msgpackPort'] = 0
	wars.append(winfo)
	jsonData['httpServices']=wars

	jf = open(jsonfile,"w")
	jf.write(json.dumps(jsonData, sort_keys=True, indent=4))
	jf.close()


	cd = cd + ' -t $JettyTempPath'
#	cd = cd + ' -addjar {0}/jp.go.nict.isp.wisdom2013.api.jar,{1}/jp.go.nict.langrid.service.common_1_2.jar '.format(deploypath,deploypath)
	
	local('cat {0}  | sed -e \'s#^\\(java.* $EmbeddedJettyPath\\)\\([^>]*\\)#\\1 {1} #g\' | sed -e \'s#^\\(JettyTempPath=\\)\\(.*\\)#\\1{2}{3}/tmp #g\' > {4}'
	.format(sf,cd, env['deploy']['deploy.path'],"proxy",sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))
	
	sf = '{0}/{1}'.format(repos_work,env['proxyserver']['scripts.stop'])
	local('cat {0} | sed -e \'s#^\\([^/]*\\)\\(.*pid\\)#\\1 {1}{2}/tmp/proxy.pid #g\' > {3}'.format(sf, env['deploy']['deploy.path'],"proxy",sf+".work"))
	local('cp {0} {1}'.format(sf+".work",sf))

@roles('proxys')
@parallel
def _deployproxy():
	deploypath = env['deploy']['deploy.path'] + "proxy" 
	run('mkdir -p {0}/tmp'.format(deploypath))
	run('mkdir -p {0}/{1}/war'.format(deploypath,env['proxyserver']['target.project']))
	put('{0}/{1}/war/*'.format(repos_work,env['proxyserver']['target.project']),'{0}/{1}/war'.format(deploypath,env['proxyserver']['target.project']))
	#jetty
	run('mkdir -p {0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
	put('{0}/{1}/*'.format(repos_work,env['jetty']['jetty.project']),'{0}/{1}'.format(deploypath,env['jetty']['jetty.project']))
#	put('{0}/rasc_build/build/jp.go.nict.isp.wisdom2013.api.jar'.format(repos_work),'{0}/jp.go.nict.isp.wisdom2013.api.jar'.format(deploypath))
#	put('{0}/jp.go.nict.isp.wisdom2013.lib/lib/langrid/jp.go.nict.langrid.service.common_1_2.jar'.format(repos_work),'{0}/jp.go.nict.langrid.service.common_1_2.jar'.format(deploypath))
	run('chmod 777 {0}/{1}'.format(deploypath,env['proxyserver']['scripts.start']))
	run('chmod 777 {0}/{1}'.format(deploypath,env['proxyserver']['scripts.stop']))

#####################################
# startproxy  Proxyを起動
#####################################
def startproxy():
	proxy =  eval(env['proxyserver']['proxy'])
	env.roledefs.update({'proxys' : _make_host_list(proxy['host'])})
	execute(_startproxy)

#####################################
# stopproxy  Proxyを停止
#####################################
def stopproxy():
	proxy =  eval(env['proxyserver']['proxy'])
	env.roledefs.update({'proxys' : _make_host_list(proxy['host'])})
	execute(_stopproxy)

@roles('proxys')
@parallel
def _startproxy():
	deploypath = env['deploy']['deploy.path'] + "proxy" + "/"
	run('{0}{1}'.format(deploypath,env['proxyserver']['scripts.start']),shell=True,pty=False)

@roles('proxys')
@parallel
def _stopproxy():
	deploypath = env['deploy']['deploy.path'] + "proxy" + "/"
	run('{0}{1}'.format(deploypath,env['proxyserver']['scripts.stop']),shell=True,pty=False)

#####################################
# cleanproxy  ProxyをClean
#####################################
def cleanproxy():
	proxy =  eval(env['proxyserver']['proxy'])
	execute(stopproxy)
	env.roledefs.update({'proxys' : _make_host_list(proxy['host'])})
	execute(_cleanproxy)

@roles('proxys')
@parallel
def _cleanproxy():
	proxy =  eval(env['proxyserver']['proxy'])
	deploypath = env['deploy']['deploy.path'] + "proxy" + "/"
	run('rm -rf {0}'.format(deploypath))

