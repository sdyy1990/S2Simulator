#!/usr/bin/python
import sys
import os
import random
import subprocess
def uniform(N,maxx):
    ll = [ (0,maxx) ]
    for i in range(1,N):
    #    print ll
        a = ll[0][:]
        ll.remove(a)
        t1 = random.randint(maxx/(3*i),a[1]-maxx/(3*i))
        ll.append( (a[0],t1) )
        ll.append( (a[0]+t1, a[1]-t1))
        ll.sort( key = lambda tup: tup[1], reverse=True )
    st = random.randint(0,maxx)
    ans =  [ (st+a[0]) % maxx for a in ll ] 
    random.shuffle(ans)
    return ans;
def rrrandom(N,maxx):
    s  = set([])
    ans = []
    for i in range(N):
        k = random.randint(0,maxx)
        while k in s:
            k = random.randint(0,maxx)
        ans.append(k)
    return ans
    
def genTopo(N,Tname):
    '''generate topology, with N node, Dlines, write Topo into Tname'''
    global Coor
    global Neighbour
    deg = []
    assignment = []
    for i in range(HH%N):
        deg.append(HH/N+1)
        for w in range(HH/N+1):
           assignment.append(i)

    for i in range(HH%N,N):
        deg.append(HH/N)
        for w in range(HH/N):
            assignment.append(i)
    Coor = [ [] for i in range(D)]
    Neighbour = [ set([]) for i in range(N)]
    si = set([])
    for i in range(D):
        Coor[i] = uniform(N,1000000)[:]
 #       print Coor[i]
        air = Coor[i][:]
        air.sort();
        air.append(air[0])
        for j in range(N):
            idx = Coor[i].index(air[j])
            idy = Coor[i].index(air[j+1])
            Neighbour[idx].add(idy)
            Neighbour[idy].add(idx)
#    for i in range(N):
#        for t in range(D):
#            Cout.write("%d "%Coor[t][i]);
#        Cout.write("\n")
#    Cout.close()
    
    for i in range(N):
        deg[i] += len(Neighbour[i])
    te1 = sum([len(k) for k in Neighbour]) /2;
    notfull = [a for a in range(N) if deg[a] < PP]
    global nxthpscnt
    nxthpscnt = [ [0 for i in range(N)] for j in range(N)]
    for i in range(N):
        for j in range(N):
            if i!=j:
                hopCount(i,j)
#    print nxthpscnt;
    stmp = [(nxthpscnt[i][j] + nxthpscnt[j][i],i,j) for i in range(N) for j in range(i)]
    sel = [stmp[x] for x in range(len(stmp)) if (stmp[x][0]>0 and (stmp[x][1] not in Neighbour[stmp[x][2]]))]
    sel.sort(key = lambda tup:tup[0], reverse = True)
#    print sel
    pos = 0
    while (len(notfull)>1) and pos < len(sel):
        (t,a,b) = sel[pos]
        pos += 1
        if (deg[a] ==PP or deg[b] ==PP): continue
        deg[a] += 1
        deg[b] += 1
        Neighbour[a].add(b)
        Neighbour[b].add(a)
        notfull = [a for a in range(N) if deg[a] < PP]
    R = [(a,b) for a in notfull for b in notfull if (a not in Neighbour[b]) and a!=b]
#    print stmp
    while len(R) > 0:
        print len(R)
        (a,b) = R[random.randint(0,len(R)-1)]
        deg[a] += 1;
        deg[b] += 1;
        Neighbour[a].add(b)
        Neighbour[b].add(a)
        notfull = [a for a in range(N) if deg[a] < PP]
        R = [(a,b) for a in notfull for b in notfull if (a not in Neighbour[b])and a!=b]
        te1+=1
    Cout = file(Tname,'w')
    Cout.write("N %d\n"%N)
    Cout.write("L %d\n"%D)
    Cout.write("D 1\n");

    edgcnt = 0;
    for i in range(N):
        for j in Neighbour[i]:
            if j>i:
                Cout.write("E %d %d 1\n"%(i+1,j+1))
                edgcnt +=1
    for i in range(N):
        Cout.write("C %d "%i)
        for t in range(D):
            Cout.write("%d "%Coor[t][i])
        Cout.write("\n")
    for i in range(HH):
        Cout.write("H %d %d %.6lf\n"%(i+1,assignment[i]+1,random.random()))
    print edgcnt
    print "totedge %d avedegree=%lf unusedport=%d"%(edgcnt,edgcnt*2.0/N,PP*N-sum(deg))
    Cout.close()

def CoorDist(x,y): 
    sq = [(a-b)*(a-b) for a,b in zip(x,y)]
    return sum(sq)
def circledist(x):
    if x<0:
        x = -x
    if x<500000:
        return x
    else:
        return 1000000-x
def hopCount(st,ed,faa=False):
    ans = 0
    path = [st,ed]
    while st != ed:
        mint = 1e100
        for nxt in Neighbour[st]:
            dists = [circledist(Coor[i][ed]-Coor[i][nxt]) for i in range(D)]
            cd = min(dists)
            #cd = CoorDist(Coor[nxt][minidx],Coor[ed][minidx])
            if cd < mint:
                mint  = cd
                tohop = nxt
        ans += 1
        if (ans > 100) :
            print "  ERR routing",st," ",ed,
        edgeUseCnt[st][tohop] += 1
        st = tohop
        path.append(tohop)
    if (len(path)>2):
        for i in range(len(path)-2):
            nxthpscnt[path[i]][path[i+2]] += 1
    pathdistri[len(path)]+=1
    return len(path) 



def getRangeOnCircle(d,center,neigh):
    ''' splits the circle on dimention d, out put the interval length of neigh takes'''
    coc = Coor[d][center];
    if (coc < Coor[d][neigh]): coc += 1000000;
    sst = [coc]
    for i in Neighbour[neigh]:
       if Coor[d][i]<Coor[d][neigh]:
           sst.append(Coor[d][i]+1000000)
       elif Coor[d][i]>Coor[d][neigh]:
           sst.append(Coor[d][i])
    return (1000000-max(sst)+min(sst))*0.000001


#main
if __name__ == "__main__":
    ''' arg: switch_count, host_count , port_count, testcasecount, output: average routing hopcount'''
    random.seed()
    global D
    global PP
    global HH
    global edgeUseCnt;
    global pathdistri;
    pathdistri = [0]*100;
    NN = int(sys.argv[1])
    HH = int(sys.argv[2])
    tTT = int(sys.argv[4])
    PP = int(sys.argv[3]);
    edgeUseCnt = []
    for i in range(NN):
        edgeUseCnt.append([])
        for j in range(NN):
            edgeUseCnt[i].append(0)
    ww = HH/NN;
    if (HH % NN >0): ww += 1
    D = int((PP+0.00000001 - ww)*0.5)
    totave = 0.0
    for TT in range(tTT) :
        genTopo(NN,"%d.%d.%d.%d.MSG1.BU3.Topo"%(HH,NN,PP,TT))
        tot = 0
        for i in range(NN): 
            for j in range(NN):
                edgeUseCnt[i][j] = 0
        
        for i in range(NN):
            for j in range(NN):
                    if len(sys.argv) < 8 :
                        pass
                    else:
                        tot += hopCount(i,j,faa=True)
        avet =  tot *1.0 / (NN*(NN-1))
        totave += avet
        print avet
        aaaa = 0 
        for i in range(NN):
            for j in Neighbour[i]:
                if (j>i): 
                 if (len(sys.argv)>8) and edgeUseCnt[i][j]+edgeUseCnt[j][i] > 0: 
                    print "%d,%d,%d;"%(i,j,edgeUseCnt[i][j]+edgeUseCnt[j][i]),
                    aaaa+= edgeUseCnt[i][j] + edgeUseCnt[j][i]
            #        for d in range(D):
            #            print "%.6lf,%.6lf,"%(getRangeOnCircle(d,i,j),getRangeOnCircle(d,j,i)),;
            #        print
             #       continue
                    toptmp = []
                    for d in range(D):
                        toptmp.append(circledist(Coor[d][i]-Coor[d][j])*0.000001);
                    toptmp.sort()
                    for d in range(D):
                        print "%.6lf,"%(toptmp[d]),;
                    print
    print aaaa
    print tot
    totave = totave *1.0/tTT
    print "ave routing path length of %d node on %d layers is %f" % (NN,D,totave)
    print pathdistri;

