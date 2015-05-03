clear all
close all

% ---- RF threshold -----
% tau=[-10,-1,-0.1,0,1,5,10];
% MAP = [0.0221,0.0283,0.0684,0.5075,0.4875,0.4394,0.3873];
% PRES = [0.1813,0.2143,0.3832,0.6087, 0.6183, 0.6023, 0.5682];
% Recall =[0.1882,0.2219,0.3908,0.6118, 0.6223, 0.6072, 0.5736];
tau=    [-10,-1,0,1,5,10];
MAP =   [0.0221,0.0283, 0.5075, 0.4875, 0.4394, 0.3873];
PRES =  [0.1813,0.2143, 0.6087, 0.6183, 0.6023, 0.5682];
Recall =[0.1882,0.2219, 0.6118, 0.6223, 0.6072, 0.5736];

MAP_PQ =   [0.1396, 0.2578, 0.4241, 0.4360, 0.3949, 0.3446];
Recall_PQ =[0.4604, 0.5544, 0.6165, 0.6224, 0.6020, 0.5628];

% ------------------ RF thresold plots -------------------
figure
hold on
grid
set(gca, 'FontName', 'Arial')
set(gca, 'FontSize', 19)
xlabel('\tau','FontSize', 30)
ylabel('Performance','FontSize', 30)
plot(tau, MAP,'-b.', 'LineWidth',2,'MarkerSize',20)
xlim([-10, 10.1])

hold on
plot(tau, Recall,'-r.', 'LineWidth',2,'MarkerSize',20)

plot(tau, MAP_PQ,'--b.', 'LineWidth',2,'MarkerSize',20)
plot(tau, Recall_PQ,'--r.', 'LineWidth',2,'MarkerSize',20)

line(xlim,[0.1181,0.1181],'LineWidth', 1 ,'color','g','LineStyle',':')
line(xlim,[0.4385,0.4385],'LineWidth', 1 ,'color','m','LineStyle',':')

h=legend('MAP, OracularQuery', 'Avg. Recall, OracularQuery', 'MAP, OracularQuery-PQ', 'Avg. Recall, OracularQuery-PQ','baseline MAP', 'baseline Recall','FontSize',20)
set(h,'FontSize',16);

% ----------------- Thresold plots for DF, QTF, PRF -----------------

tauDF = [0, 1, 5, 10];
MAPDF =   [0.0053, 0.0696, 0.0873, 0.0973];
MAPQFT =  [0.1624, 0.1620, 0.1572, 0.1520];
MAPPRF = [0.1494, 0.1461, 0.1347, 0.1258];
figure
hold on
grid
set(gca, 'FontName', 'Arial')
set(gca, 'FontSize', 19)
xlabel('\tau','FontSize', 30)
ylabel('Performance','FontSize', 30)
plot(tauDF, MAPDF,'-m.', 'LineWidth',2,'MarkerSize',20)
%  xlim([-10, 10.1])
%  ylim([0 0.45])

hold on
plot(tauDF, MAPQFT,'-b.', 'LineWidth',2,'MarkerSize',20)
plot(tauDF, MAPPRF,'-k.', 'LineWidth',2,'MarkerSize',20)
plot(0, 0.1581,'-r.', 'LineWidth',2,'MarkerSize',20)
% hold on
% line(xlim,[0.1581,0.1581],'LineWidth', 2 ,'color','g','LineStyle','--')

a=legend('DF(t)>\tau','QTF(t)>\tau', 'PRF(t)>\tau', 'IPC Title')
set(a,'FontSize',16);
