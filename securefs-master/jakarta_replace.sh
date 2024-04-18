git diff |grep '^+.*import jakarta'| sed -r 's/^\+.*(import +jakarta.[^.]+).*$/\1/' |sort | uniq

P=$(git diff |grep '^+.*import jakarta'| sed -r 's/^\+.*(import +jakarta.[^.]+).*$/\1/' |sort | uniq | sed 's/import jakarta.//')

for p in $P; do 
	echo PROCESSING: $p
	git diff |grep -P -- '^-.*import +.*'$p | sed -r 's/^\-.*(import +.*'$p')\..*$/\1/' |sort | uniq
done
