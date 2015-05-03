% clear all
close all

M = load('./HistoRank/1stRank.txt');
%'./RF-DF-score/PAC-100-rf-df-score.txt'
binranges = 0:10:90;
[bincounts] = histc(M,binranges)
figure
bar(binranges,bincounts,'histc')
% hist(M);
grid on
%set(gca, 'YTick', 0:100:550)
hold on
set(gca, 'FontName', 'Arial')
set(gca, 'FontSize', 19)
xlabel('Rank of the 1st Rel. Doc.','FontSize', 30)
ylabel('Frequency','FontSize', 30)

